import { describe, expect, it } from "vitest";
import type { AxiosInstance } from "axios";
import {
  containsPoint,
  DeliveryZone,
  mayShareTrip,
  zonesContaining,
} from "../../main/ts/domain/model/deliveryZone";
import { GeoPoint } from "../../main/ts/domain/model/geo";
import { planDeliveryPlacement, routeKm, Trip } from "../../main/ts/domain/model/trip";
import { DeliveryGate } from "../../main/ts/application/deliveryGate";
import { ScheduleOrder } from "../../main/ts/application/scheduleOrder";
import { PolicyResolver } from "../../main/ts/application/policyResolver";
import { InMemoryBookingRepository } from "../../main/ts/infrastructure/outbound/inMemoryBookingRepository";
import { InMemoryTripRepository } from "../../main/ts/infrastructure/outbound/inMemoryTripRepository";
import { HttpRestaurantZones } from "../../main/ts/infrastructure/outbound/httpRestaurantZones";
import { CapacityPolicy } from "../../main/ts/domain/model/capacityPolicy";
import { OrderClient } from "../../main/ts/domain/ports/orderClient";

// The restaurant sits at the origin. Two square zones touch it from the west and the east, and
// a third one, OVERLAP, straddles the border between them around (0.01, 0).
const R: GeoPoint = { lat: 0, lng: 0 };
const square = (id: string, lng0: number, lng1: number, lat0 = -0.05, lat1 = 0.05): DeliveryZone => ({
  id,
  name: id,
  boundary: [
    { lat: lat0, lng: lng0 },
    { lat: lat0, lng: lng1 },
    { lat: lat1, lng: lng1 },
    { lat: lat1, lng: lng0 },
  ],
});
const WEST = square("west", -0.05, 0);
const EAST = square("east", 0, 0.05);
const OVERLAP = square("overlap", -0.01, 0.01, 0.005, 0.015);
const ZONES = [WEST, EAST, OVERLAP];

// Two addresses ~220 m apart across the west/east border: close enough to batch on distance alone.
const WEST_POINT: GeoPoint = { lat: 0.02, lng: -0.001 };
const EAST_POINT: GeoPoint = { lat: 0.02, lng: 0.001 };
const WEST_NEAR: GeoPoint = { lat: 0.021, lng: -0.002 };
const OUTSIDE: GeoPoint = { lat: 0.06, lng: 0.001 };
const OUTSIDE_TOO: GeoPoint = { lat: 0.061, lng: 0.002 };
const IN_OVERLAP_WEST: GeoPoint = { lat: 0.01, lng: -0.002 };
const IN_OVERLAP_EAST: GeoPoint = { lat: 0.01, lng: 0.002 };

const LIMITS = { maxDetourKm: 1, maxTripKmBudget: 30, maxTripsPerSlot: 3 };

const trip = (id: string, stops: GeoPoint[]): Trip => ({
  id,
  restaurantId: "r1",
  slotStartEpochMs: 0,
  origin: R,
  stops: stops.map((location, i) => ({ bookingId: `b${id}${i}`, location })),
  totalKm: routeKm(R, stops),
  progress: null,
});

describe("containsPoint", () => {
  it("finds a point inside and outside a square", () => {
    expect(containsPoint(WEST.boundary, WEST_POINT)).toBe(true);
    expect(containsPoint(WEST.boundary, EAST_POINT)).toBe(false);
  });

  it("handles a concave polygon", () => {
    // An L shape: the notch at the top right is outside.
    const l: GeoPoint[] = [
      { lat: 0, lng: 0 },
      { lat: 0, lng: 2 },
      { lat: 1, lng: 2 },
      { lat: 1, lng: 1 },
      { lat: 2, lng: 1 },
      { lat: 2, lng: 0 },
    ];
    expect(containsPoint(l, { lat: 0.5, lng: 1.5 })).toBe(true);
    expect(containsPoint(l, { lat: 1.5, lng: 0.5 })).toBe(true);
    expect(containsPoint(l, { lat: 1.5, lng: 1.5 })).toBe(false);
  });

  it("returns every overlapping zone", () => {
    expect(zonesContaining(ZONES, IN_OVERLAP_WEST)).toEqual(new Set(["west", "overlap"]));
    expect(zonesContaining(ZONES, OUTSIDE).size).toBe(0);
  });
});

describe("mayShareTrip", () => {
  it("allows anything when the restaurant has no zones", () => {
    expect(mayShareTrip([], [WEST_POINT], EAST_POINT)).toBe(true);
  });

  it("requires a zone common to every stop and the new point", () => {
    expect(mayShareTrip(ZONES, [WEST_POINT], WEST_NEAR)).toBe(true);
    expect(mayShareTrip(ZONES, [WEST_POINT], EAST_POINT)).toBe(false);
  });

  it("lets overlapping zones bridge a border", () => {
    expect(mayShareTrip(ZONES, [IN_OVERLAP_WEST], IN_OVERLAP_EAST)).toBe(true);
    // ...but not once a stop is only in WEST: there is no zone common to all three.
    expect(mayShareTrip(ZONES, [IN_OVERLAP_WEST, WEST_POINT], IN_OVERLAP_EAST)).toBe(false);
  });

  it("never batches an address outside every zone", () => {
    expect(mayShareTrip(ZONES, [OUTSIDE], OUTSIDE_TOO)).toBe(false);
    expect(mayShareTrip(ZONES, [WEST_POINT], OUTSIDE)).toBe(false);
  });
});

describe("planDeliveryPlacement with zones", () => {
  it("without zones, joins the nearby trip across the border as before", () => {
    const plan = planDeliveryPlacement(R, [trip("A", [WEST_POINT])], EAST_POINT, LIMITS);
    expect(plan.kind).toBe("join");
  });

  it("with zones, opens a new trip instead of crossing the border", () => {
    const plan = planDeliveryPlacement(R, [trip("A", [WEST_POINT])], EAST_POINT, LIMITS, ZONES);
    expect(plan.kind).toBe("new");
  });

  it("with zones, still joins a trip of the same zone", () => {
    const plan = planDeliveryPlacement(R, [trip("A", [WEST_POINT])], WEST_NEAR, LIMITS, ZONES);
    expect(plan.kind).toBe("join");
  });

  it("picks the trip of the same zone even when another trip is closer", () => {
    const sameZone = trip("W", [{ lat: 0.03, lng: -0.004 }]);
    const closerOtherZone = trip("E", [EAST_POINT]);
    const plan = planDeliveryPlacement(R, [closerOtherZone, sameZone], WEST_POINT, LIMITS, ZONES);
    expect(plan.kind === "join" && plan.trip.id).toBe("W");
  });

  it("detour still applies inside a zone", () => {
    const far: GeoPoint = { lat: -0.045, lng: -0.045 };
    const plan = planDeliveryPlacement(R, [trip("A", [WEST_POINT])], far, LIMITS, ZONES);
    expect(plan.kind).toBe("new");
  });

  it("an address outside every zone gets a trip of its own, or none when the slot is full", () => {
    const trips = [trip("A", [OUTSIDE])];
    expect(planDeliveryPlacement(R, trips, OUTSIDE_TOO, LIMITS, ZONES).kind).toBe("new");
    expect(planDeliveryPlacement(R, trips, OUTSIDE_TOO, { ...LIMITS, maxTripsPerSlot: 1 }, ZONES)).toEqual({
      kind: "rejected",
      reason: "no_trips_left",
    });
  });
});

describe("ScheduleOrder with zones", () => {
  const policy: CapacityPolicy = {
    slotMinutes: 30,
    maxCapacityUnits: 10,
    opening: "11:00",
    closing: "22:00",
    prepLeadMinutes: 30,
    maxAdvanceDays: 7,
    maxTripsPerSlot: 3,
    maxTripKmBudget: 30,
    maxDetourKm: 1,
  };
  const NOW = new Date(2026, 8, 1, 8, 0).getTime();
  const SLOT = new Date(2026, 8, 1, 12, 0).getTime();
  const orders: OrderClient = {
    async place() {
      return { orderId: "o", status: "PENDING" };
    },
    async advance() {},
    async getStatus() {
      return "PENDING";
    },
  };

  const deliverTo = (location: GeoPoint) => ({
    restaurantId: "r1",
    customerId: "c1",
    orderType: "DELIVERY" as const,
    items: [{ menuItemId: "m", quantity: 1 }],
    slotStartEpochMs: SLOT,
    deliveryAddress: "via A",
    deliveryLocation: location,
  });

  it("puts two close addresses in different zones on separate trips", async () => {
    const gate = new DeliveryGate(
      { getPolicy: async () => policy },
      { get: async () => R },
      undefined,
      { get: async () => ZONES },
    );
    const schedule = new ScheduleOrder(
      new PolicyResolver({ getPolicy: async () => null }, policy),
      new InMemoryBookingRepository(),
      new InMemoryTripRepository(),
      orders,
      gate,
      () => NOW,
    );

    const first = await schedule.execute(deliverTo(WEST_POINT));
    const second = await schedule.execute(deliverTo(EAST_POINT));
    const third = await schedule.execute(deliverTo(WEST_NEAR));

    expect(second.tripId).not.toBe(first.tripId);
    expect(third.tripId).toBe(first.tripId);
  });
});

describe("HttpRestaurantZones", () => {
  const fakeHttp = (status: number, data: unknown) =>
    ({ get: async () => ({ status, data }) }) as unknown as AxiosInstance;

  it("parses the zones and drops malformed ones", async () => {
    const client = new HttpRestaurantZones(
      "",
      fakeHttp(200, {
        result: [
          { id: "west", name: "West", boundary: WEST.boundary },
          { id: "bad", name: "Two points", boundary: WEST.boundary.slice(0, 2) },
        ],
        code: 200,
      }),
    );
    const zones = await client.get("r1");
    expect(zones.map((z) => z.id)).toEqual(["west"]);
    expect(zones[0].boundary).toHaveLength(4);
  });

  it("returns no zones for an unknown restaurant", async () => {
    expect(await new HttpRestaurantZones("", fakeHttp(404, {})).get("r1")).toEqual([]);
  });

  it("throws on a server error rather than ignoring the zones", async () => {
    await expect(new HttpRestaurantZones("", fakeHttp(500, {})).get("r1")).rejects.toThrow(/HTTP 500/);
  });
});
