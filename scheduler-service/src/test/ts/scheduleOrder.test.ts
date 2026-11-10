import { beforeEach, describe, expect, it } from "vitest";
import { ScheduleOrder } from "../../main/ts/application/scheduleOrder";
import { AdvanceScheduledOrder } from "../../main/ts/application/advanceScheduledOrder";
import { TransitionScheduler } from "../../main/ts/application/transitionScheduler";
import { PolicyResolver } from "../../main/ts/application/policyResolver";
import { DeliveryGate } from "../../main/ts/application/deliveryGate";
import { AppError } from "../../main/ts/application/errors";
import { InMemoryBookingRepository } from "../../main/ts/infrastructure/outbound/inMemoryBookingRepository";
import { InMemoryTripRepository } from "../../main/ts/infrastructure/outbound/inMemoryTripRepository";
import { CapacityPolicy } from "../../main/ts/domain/model/capacityPolicy";
import { GeoPoint } from "../../main/ts/domain/model/geo";
import { OrderClient, OrderServiceError, PlaceOrderInput, PlacedOrder } from "../../main/ts/domain/ports/orderClient";

const policy: CapacityPolicy = {
  slotMinutes: 30,
  maxCapacityUnits: 3,
  opening: "11:00",
  closing: "22:00",
  prepLeadMinutes: 30,
  maxAdvanceDays: 7,
  maxTripsPerSlot: 2,
  maxTripKmBudget: 20,
  maxDetourKm: 1,
};

const RESTAURANT: GeoPoint = { lat: 45.0, lng: 9.0 };
const NEAR: GeoPoint = { lat: 45.05, lng: 9.0 }; // ~5.6 km north
const EAST: GeoPoint = { lat: 45.0, lng: 9.05 }; // ~3.9 km east, ~6.8 km from NEAR
const NEAR_TOO: GeoPoint = { lat: 45.05, lng: 9.001 }; // ~80 m from NEAR
const FAR: GeoPoint = { lat: 46.0, lng: 9.0 }; // ~111 km

class FakeOrders implements OrderClient {
  placed: PlaceOrderInput[] = [];
  advanced: string[] = [];
  placeError: Error | null = null;
  private seq = 0;

  async place(input: PlaceOrderInput): Promise<PlacedOrder> {
    if (this.placeError) throw this.placeError;
    this.placed.push(input);
    return { orderId: `order-${++this.seq}`, status: "PENDING" };
  }

  async advance(orderId: string): Promise<void> {
    this.advanced.push(orderId);
  }
}

const NOW = new Date(2026, 8, 1, 8, 0).getTime();
const SLOT = new Date(2026, 8, 1, 12, 0).getTime();
const clock = () => NOW;

let repo: InMemoryBookingRepository;
let trips: InMemoryTripRepository;
let orders: FakeOrders;
let schedule: ScheduleOrder;

function build(p: CapacityPolicy = policy, location: GeoPoint | null = RESTAURANT, configured = true) {
  const policies = new PolicyResolver({ getPolicy: async () => null }, p);
  const delivery = new DeliveryGate(
    { getPolicy: async () => (configured ? p : null) },
    { get: async () => location },
  );
  schedule = new ScheduleOrder(policies, repo, trips, orders, delivery, clock);
}

beforeEach(() => {
  repo = new InMemoryBookingRepository();
  trips = new InMemoryTripRepository();
  orders = new FakeOrders();
  build();
});

const takeaway = (quantity = 1, customerId = "c1") => ({
  restaurantId: "r1",
  customerId,
  orderType: "TAKEAWAY" as const,
  items: [{ menuItemId: "m1", quantity }],
  slotStartEpochMs: SLOT,
  customerName: "Ada",
});

const delivery = (point: GeoPoint, customerId = "c1") => ({
  restaurantId: "r1",
  customerId,
  orderType: "DELIVERY" as const,
  items: [{ menuItemId: "m1", quantity: 1 }],
  slotStartEpochMs: SLOT,
  deliveryAddress: "Via Roma 1",
  deliveryLocation: point,
});

describe("ScheduleOrder: kitchen capacity", () => {
  it("records the order's weight as the sum of item quantities and places it", async () => {
    const result = await schedule.execute(takeaway(2));
    expect(orders.placed[0].pickupTime).toBe(String(SLOT));
    expect((await repo.findById(result.bookingId))?.weight).toBe(2);
    expect(result.tripId).toBeNull();
  });

  it("rejects with 409 when the summed weight would exceed maxCapacityUnits", async () => {
    await schedule.execute(takeaway(2));
    await expect(schedule.execute(takeaway(2, "c2"))).rejects.toMatchObject({ status: 409 });
    expect(orders.placed).toHaveLength(1);
  });

  it("accepts an order that exactly fills the remaining units", async () => {
    await schedule.execute(takeaway(2));
    await expect(schedule.execute(takeaway(1, "c2"))).resolves.toMatchObject({ status: "CONFIRMED" });
  });

  it("releases the units when order-service rejects the order", async () => {
    orders.placeError = new OrderServiceError("Empty items", 400);
    await expect(schedule.execute(takeaway(3))).rejects.toMatchObject({ status: 400 });
    orders.placeError = null;
    await expect(schedule.execute(takeaway(3, "c2"))).resolves.toMatchObject({ status: "CONFIRMED" });
  });

  it("requires a deliveryAddress for DELIVERY orders", async () => {
    await expect(schedule.execute({ ...delivery(NEAR), deliveryAddress: undefined })).rejects.toMatchObject({ status: 400 });
  });
});

describe("ScheduleOrder: delivery locations", () => {
  it.each([
    ["missing", undefined],
    ["latitude out of range", { lat: 200, lng: 9 }],
    ["longitude out of range", { lat: 45, lng: 181 }],
    ["not finite", { lat: Number.NaN, lng: 9 }],
  ])("rejects a %s deliveryLocation with 400", async (_label, point) => {
    await expect(
      schedule.execute({ ...delivery(NEAR), deliveryLocation: point as GeoPoint | undefined }),
    ).rejects.toMatchObject({ status: 400 });
    expect(orders.placed).toHaveLength(0);
  });
});

describe("ScheduleOrder: delivery trips", () => {
  it("opens a new trip for the first delivery and returns its id", async () => {
    const result = await schedule.execute(delivery(NEAR));
    const stored = await trips.findBySlot("r1", SLOT);
    expect(stored).toHaveLength(1);
    expect(result.tripId).toBe(stored[0].id);
    expect(stored[0].stops.map((s) => s.bookingId)).toEqual([result.bookingId]);
    expect(stored[0].totalKm).toBeCloseTo(2 * 5.56, 1);
  });

  it("joins an existing trip when the detour is small", async () => {
    const first = await schedule.execute(delivery(NEAR, "c1"));
    const second = await schedule.execute(delivery(NEAR_TOO, "c2"));
    expect(second.tripId).toBe(first.tripId);
    const [trip] = await trips.findBySlot("r1", SLOT);
    expect(trip.stops).toHaveLength(2);
  });

  it("opens a second trip when the detour is too large and a trip is free", async () => {
    await schedule.execute(delivery(NEAR, "c1"));
    const second = await schedule.execute(delivery(EAST, "c2"));
    expect(await trips.findBySlot("r1", SLOT)).toHaveLength(2);
    expect(second.tripId).not.toBeNull();
  });

  it("rejects with 409 when no trip is left in the slot", async () => {
    build({ ...policy, maxTripsPerSlot: 1, maxCapacityUnits: 10 });
    await schedule.execute(delivery(NEAR, "c1"));
    await expect(schedule.execute(delivery(EAST, "c2"))).rejects.toMatchObject({ status: 409 });
    expect(orders.placed).toHaveLength(1);
  });

  it("rejects with 400 when a single round trip exceeds the km budget", async () => {
    await expect(schedule.execute(delivery(FAR))).rejects.toMatchObject({ status: 400 });
    expect(orders.placed).toHaveLength(0);
  });

});

describe("TransitionScheduler and AdvanceScheduledOrder", () => {
  it("advances a due booking to PREPARING once, then nothing is due", async () => {
    await schedule.execute(takeaway(1));
    const timer = new TransitionScheduler(repo, orders, 1000, () => SLOT - 30 * 60_000 + 1);
    expect(await timer.tick()).toEqual({ advanced: 1, failed: 0 });
    expect(await timer.tick()).toEqual({ advanced: 0, failed: 0 });
  });

  it("forwards a manual advance and reflects it on the booking", async () => {
    const { orderId } = await schedule.execute(takeaway(1));
    const result = await new AdvanceScheduledOrder(repo, orders, clock).execute(orderId);
    expect(result.bookingStatus).toBe("PREPARING");
  });
});

describe("delivery gate", () => {
  const delivery = (quantity = 1) => ({
    restaurantId: "r1",
    customerId: "c1",
    orderType: "DELIVERY" as const,
    items: [{ menuItemId: "m1", quantity }],
    slotStartEpochMs: SLOT,
    deliveryAddress: "via Roma 1",
    deliveryLocation: NEAR,
  });

  it("rejects deliveries when the restaurant has no location", async () => {
    build(policy, null);
    await expect(schedule.execute(delivery())).rejects.toMatchObject({ status: 409 });
    expect(orders.placed).toHaveLength(0);
  });

  it("rejects deliveries when the restaurant has no configured capacity policy", async () => {
    build(policy, RESTAURANT, false);
    await expect(schedule.execute(delivery())).rejects.toMatchObject({ status: 409 });
    expect(orders.placed).toHaveLength(0);
  });

  it("still accepts takeaway orders without a location or configured policy", async () => {
    build(policy, null, false);
    const result = await schedule.execute(takeaway());
    expect(result.status).toBe("CONFIRMED");
  });

  it("returns the tripId on the booking for a delivery", async () => {
    const result = await schedule.execute(delivery());
    expect(result.tripId).not.toBeNull();
    const booking = await repo.findById(result.bookingId);
    expect(booking?.delivery?.tripId).toBe(result.tripId);
  });
});

