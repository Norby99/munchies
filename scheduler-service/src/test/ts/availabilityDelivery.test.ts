import { describe, expect, it } from "vitest";
import { GetAvailability } from "../../main/ts/application/getAvailability";
import { ScheduleOrder } from "../../main/ts/application/scheduleOrder";
import { DeliveryGate } from "../../main/ts/application/deliveryGate";
import { PolicyResolver } from "../../main/ts/application/policyResolver";
import { InMemoryBookingRepository } from "../../main/ts/infrastructure/outbound/inMemoryBookingRepository";
import { InMemoryTripRepository } from "../../main/ts/infrastructure/outbound/inMemoryTripRepository";
import { CapacityPolicy } from "../../main/ts/domain/model/capacityPolicy";
import { GeoPoint } from "../../main/ts/domain/model/geo";
import { OrderClient, PlacedOrder } from "../../main/ts/domain/ports/orderClient";

// One driver per slot, so a second far-off address cannot be batched into a full trip.
const policy: CapacityPolicy = {
  slotMinutes: 30,
  maxCapacityUnits: 10,
  opening: "11:00",
  closing: "22:00",
  prepLeadMinutes: 30,
  maxAdvanceDays: 7,
  maxTripsPerSlot: 1,
  maxTripKmBudget: 20,
  maxDetourKm: 1,
};

const RESTAURANT: GeoPoint = { lat: 45.0, lng: 9.0 };
const NEAR: GeoPoint = { lat: 45.05, lng: 9.0 }; // ~5.6 km north, the existing trip's stop
const NEAR_TOO: GeoPoint = { lat: 45.05, lng: 9.001 }; // ~80 m from NEAR: detour ~0
const EAST: GeoPoint = { lat: 45.0, lng: 9.05 }; // ~3.9 km east: needs a second trip

const NOW = new Date(2026, 8, 1, 8, 0).getTime();
const SLOT = new Date(2026, 8, 1, 12, 0).getTime();
const DATE = "2026-09-01";

const orders: OrderClient = {
  async place(): Promise<PlacedOrder> {
    return { orderId: "o", status: "PENDING" };
  },
  async advance() {},
};

function setup(location: GeoPoint | null = RESTAURANT) {
  const bookings = new InMemoryBookingRepository();
  const trips = new InMemoryTripRepository();
  const gate = new DeliveryGate(
    { getPolicy: async () => policy },
    { get: async () => location },
  );
  const policies = new PolicyResolver({ getPolicy: async () => null }, policy);
  const schedule = new ScheduleOrder(policies, bookings, trips, orders, gate, () => NOW);
  const availability = new GetAvailability(policies, bookings, trips, gate, () => NOW);
  return { schedule, availability };
}

const slotStarts = (ranges: { startEpochMs: number }[]) => ranges.map((r) => r.startEpochMs);

describe("delivery feasibility in availability", () => {
  it("drops a slot where the only trip is full and the new address needs a second trip", async () => {
    const { schedule, availability } = setup();
    await schedule.execute({
      restaurantId: "r1",
      customerId: "c1",
      orderType: "DELIVERY",
      items: [{ menuItemId: "m", quantity: 1 }],
      slotStartEpochMs: SLOT,
      deliveryAddress: "via A",
      deliveryLocation: NEAR,
    });

    const result = await availability.execute({
      restaurantId: "r1",
      dateIso: DATE,
      orderType: "DELIVERY",
      deliveryLocation: EAST,
    });

    expect(slotStarts(result.ranges)).not.toContain(SLOT);
    // Other slots have no trip yet, so a new trip is still possible there.
    expect(result.ranges.length).toBeGreaterThan(0);
  });

  it("keeps a slot where the address detours almost nothing from the existing trip", async () => {
    const { schedule, availability } = setup();
    await schedule.execute({
      restaurantId: "r1",
      customerId: "c1",
      orderType: "DELIVERY",
      items: [{ menuItemId: "m", quantity: 1 }],
      slotStartEpochMs: SLOT,
      deliveryAddress: "via A",
      deliveryLocation: NEAR,
    });

    const result = await availability.execute({
      restaurantId: "r1",
      dateIso: DATE,
      orderType: "DELIVERY",
      deliveryLocation: NEAR_TOO,
    });

    expect(slotStarts(result.ranges)).toContain(SLOT);
  });

  it("rejects a delivery availability request without a location", async () => {
    const { availability } = setup();
    await expect(
      availability.execute({ restaurantId: "r1", dateIso: DATE, orderType: "DELIVERY" }),
    ).rejects.toMatchObject({ status: 400 });
  });

  it("offers no delivery slots while the restaurant has no location", async () => {
    const { availability } = setup(null);
    const result = await availability.execute({
      restaurantId: "r1",
      dateIso: DATE,
      orderType: "DELIVERY",
      deliveryLocation: EAST,
    });
    expect(result.ranges).toEqual([]);
  });
});
