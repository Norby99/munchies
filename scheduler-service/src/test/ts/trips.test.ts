import { describe, expect, it } from "vitest";
import { haversineKm, isValidGeoPoint } from "../../main/ts/domain/model/geo";
import { cheapestInsertion, planDeliveryPlacement, routeKm, Trip } from "../../main/ts/domain/model/trip";

const R = { lat: 0, lng: 0 };
const KM_PER_DEG = 6371 * (Math.PI / 180);
const LIMITS = { maxDetourKm: 1, maxTripKmBudget: 20, maxTripsPerSlot: 2 };

const trip = (id: string, stops: { lat: number; lng: number }[]): Trip => ({
  id,
  restaurantId: "r1",
  slotStartEpochMs: 0,
  stops: stops.map((location, i) => ({ bookingId: `b${id}${i}`, location })),
  totalKm: routeKm(R, stops),
});

describe("geo", () => {
  it("haversine: one degree of latitude is about 111.2 km", () => {
    expect(haversineKm({ lat: 0, lng: 0 }, { lat: 1, lng: 0 })).toBeCloseTo(KM_PER_DEG, 2);
  });

  it("validates coordinate ranges and finiteness", () => {
    expect(isValidGeoPoint({ lat: 45, lng: 9 })).toBe(true);
    expect(isValidGeoPoint({ lat: 90, lng: -180 })).toBe(true);
    expect(isValidGeoPoint({ lat: 91, lng: 0 })).toBe(false);
    expect(isValidGeoPoint({ lat: 0, lng: 181 })).toBe(false);
    expect(isValidGeoPoint({ lat: Number.POSITIVE_INFINITY, lng: 0 })).toBe(false);
    expect(isValidGeoPoint({ lat: "45", lng: 9 })).toBe(false);
    expect(isValidGeoPoint(undefined)).toBe(false);
  });
});

describe("route km", () => {
  it("a single stop is the closed round trip", () => {
    expect(routeKm(R, [{ lat: 0, lng: 1 }])).toBeCloseTo(2 * KM_PER_DEG, 2);
  });

  it("an empty route is zero", () => {
    expect(routeKm(R, [])).toBe(0);
  });

  it("a stop on the way out adds no detour over the direct round trip", () => {
    const { position, detourKm } = cheapestInsertion(R, [{ lat: 0, lng: 1 }], { lat: 0, lng: 0.5 });
    expect(position).toBe(0);
    expect(detourKm).toBeCloseTo(0, 6);
  });
});

describe("planDeliveryPlacement", () => {
  it("opens a new trip costing the round trip when none exist", () => {
    const plan = planDeliveryPlacement(R, [], { lat: 0, lng: 0.05 }, LIMITS);
    expect(plan.kind).toBe("new");
    if (plan.kind === "new") expect(Math.abs(plan.costKm - 2 * 0.05 * KM_PER_DEG)).toBeLessThan(0.01);
  });

  it("joins the eligible trip with the smallest detour, not the first one", () => {
    const a = trip("A", [{ lat: 0, lng: 0.05 }]);
    const b = trip("B", [{ lat: 0.0005, lng: 0.052 }]);
    const plan = planDeliveryPlacement(R, [a, b], { lat: 0, lng: 0.052 }, LIMITS);
    expect(plan.kind).toBe("join");
    if (plan.kind === "join") expect(plan.trip.id).toBe("B");
  });

  it("opens a new trip instead of joining when the detour exceeds maxDetourKm", () => {
    const existing = trip("t", [{ lat: 0, lng: 0.05 }]);
    const plan = planDeliveryPlacement(R, [existing], { lat: 0.05, lng: 0 }, LIMITS);
    expect(plan.kind).toBe("new");
  });

  it("does not join when the trip budget would be exceeded", () => {
    const existing = trip("t", [{ lat: 0, lng: 0.1 }]);
    const tight = { ...LIMITS, maxTripKmBudget: routeKm(R, [{ lat: 0, lng: 0.1 }]) + 0.01 };
    const plan = planDeliveryPlacement(R, [existing], { lat: 0, lng: 0.11 }, tight);
    expect(plan.kind).not.toBe("join");
  });

  it("rejects too_far when a single round trip exceeds the budget", () => {
    expect(planDeliveryPlacement(R, [], { lat: 1, lng: 0 }, LIMITS)).toEqual({
      kind: "rejected",
      reason: "too_far",
    });
  });

  it("rejects no_trips_left when the slot is full and nothing can join", () => {
    const a = trip("a", [{ lat: 0, lng: 0.1 }]);
    const b = trip("b", [{ lat: 0.1, lng: 0 }]);
    const plan = planDeliveryPlacement(R, [a, b], { lat: -0.05, lng: -0.05 }, LIMITS);
    expect(plan).toEqual({ kind: "rejected", reason: "no_trips_left" });
  });
});
