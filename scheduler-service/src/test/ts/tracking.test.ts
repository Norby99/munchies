import { beforeEach, describe, expect, it } from "vitest";
import { AdvanceScheduledOrder } from "../../main/ts/application/advanceScheduledOrder";
import { DriverSimulator } from "../../main/ts/application/driverSimulator";
import { GetOrderTracking } from "../../main/ts/application/getOrderTracking";
import { Booking } from "../../main/ts/domain/model/booking";
import { GeoPoint, haversineKm } from "../../main/ts/domain/model/geo";
import { planDeliveryPlacement, routeKm, Trip } from "../../main/ts/domain/model/trip";
import {
  advanceTrip,
  deliveryProgress,
  departTrip,
  kmToWaypoint,
  pathBetween,
  pointAlongRoute,
  routeFromLegs,
  routeWaypoints,
  straightRoute,
} from "../../main/ts/domain/model/tripProgress";
import { OrderClient, PlacedOrder } from "../../main/ts/domain/ports/orderClient";
import { RoutePlanner } from "../../main/ts/domain/ports/routePlanner";
import { OsrmRoutePlanner } from "../../main/ts/infrastructure/outbound/osrmRoutePlanner";
import { StraightLineRoutePlanner } from "../../main/ts/infrastructure/outbound/straightLineRoutePlanner";
import { InMemoryBookingRepository } from "../../main/ts/infrastructure/outbound/inMemoryBookingRepository";
import { InMemoryTripRepository } from "../../main/ts/infrastructure/outbound/inMemoryTripRepository";

const R: GeoPoint = { lat: 0, lng: 0 };
const A: GeoPoint = { lat: 0, lng: 0.05 }; // ~5.56 km east
const B: GeoPoint = { lat: 0, lng: 0.1 }; // ~11.12 km east, past A
const DETOUR: GeoPoint = { lat: 0.02, lng: 0.02 }; // a corner off the straight line from R to A
const KM_PER_DEG = 6371 * (Math.PI / 180);
const SPEED = 30; // km/h: 0.5 km per minute
const MINUTE = 60_000;
const T0 = 1_000_000;

const makeTrip = (): Trip => ({
  id: "t1",
  restaurantId: "r1",
  slotStartEpochMs: 0,
  origin: R,
  stops: [
    { bookingId: "bA", location: A },
    { bookingId: "bB", location: B },
  ],
  totalKm: routeKm(R, [A, B]),
  progress: null,
});

const booking = (id: string, orderId: string, customerId: string, location: GeoPoint | null): Booking => ({
  id,
  restaurantId: "r1",
  customerId,
  orderId,
  orderType: location ? "DELIVERY" : "TAKEAWAY",
  slotStartEpochMs: 0,
  prepareAtEpochMs: 0,
  status: "PREPARING",
  weight: 1,
  delivery: location ? { location, tripId: "t1" } : null,
  createdAtEpochMs: 0,
  updatedAtEpochMs: 0,
});

describe("route geometry", () => {
  it("lists the closed route and measures the km to each waypoint", () => {
    const waypoints = routeWaypoints(makeTrip());
    expect(waypoints).toEqual([R, A, B, R]);
    expect(kmToWaypoint(waypoints, 0)).toBe(0);
    expect(kmToWaypoint(waypoints, 1)).toBeCloseTo(0.05 * KM_PER_DEG, 6);
    expect(kmToWaypoint(waypoints, 3)).toBeCloseTo(makeTrip().totalKm, 6);
  });

  it("interpolates inside a leg and clamps to both ends", () => {
    const waypoints = routeWaypoints(makeTrip());
    const half = pointAlongRoute(waypoints, haversineKm(R, A) / 2);
    expect(half.lng).toBeCloseTo(0.025, 6);
    expect(pointAlongRoute(waypoints, -1)).toEqual(R);
    expect(pointAlongRoute(waypoints, 1_000)).toEqual(R);
  });

  it("follows the way back after the last stop", () => {
    const waypoints = routeWaypoints(makeTrip());
    const onTheWayBack = pointAlongRoute(waypoints, haversineKm(R, B) + haversineKm(B, A));
    expect(onTheWayBack.lng).toBeCloseTo(0.05, 6);
  });
});

describe("routes", () => {
  it("joins legs into one path and records where each stop is along it", () => {
    const route = routeFromLegs([[R, DETOUR, A], [A, B], [B, R]]);
    const toA = haversineKm(R, DETOUR) + haversineKm(DETOUR, A);
    expect(route.path).toEqual([R, DETOUR, A, B, R]); // joining points are not repeated
    expect(route.stopKm).toHaveLength(2);
    expect(route.stopKm[0]).toBeCloseTo(toA, 6);
    expect(route.stopKm[1]).toBeCloseTo(toA + haversineKm(A, B), 6);
    expect(route.totalKm).toBeCloseTo(toA + haversineKm(A, B) + haversineKm(B, R), 6);
  });

  it("the straight route of a trip matches its planned km", () => {
    const trip = makeTrip();
    expect(straightRoute(trip).totalKm).toBeCloseTo(trip.totalKm, 6);
  });

  it("rejects a route without legs or with an empty leg", () => {
    expect(() => routeFromLegs([])).toThrow(RangeError);
    expect(() => routeFromLegs([[R, A], []])).toThrow(RangeError);
  });

  it("cuts the part of a path between two distances", () => {
    const path = [R, A, B];
    const cut = pathBetween(path, 1, haversineKm(R, B) - 1);
    expect(cut).toHaveLength(3);
    expect(cut[0].lng).toBeCloseTo(1 / KM_PER_DEG, 6);
    expect(cut[1]).toEqual(A);
    expect(cut[2].lng).toBeCloseTo(0.1 - 1 / KM_PER_DEG, 6);
    expect(pathBetween(path, 5, 5)).toHaveLength(1);
  });
});

describe("OsrmRoutePlanner", () => {
  const step = (...points: GeoPoint[]) => ({ geometry: { coordinates: points.map((p) => [p.lng, p.lat]) } });
  const fakeHttp = (status: number, data: unknown) => {
    const calls: { url: string; params: unknown }[] = [];
    const http = {
      get: async (url: string, options: { params: unknown }) => {
        calls.push({ url, params: options.params });
        return { status, data };
      },
    };
    return { calls, planner: new OsrmRoutePlanner("http://osrm", http as never) };
  };

  it("asks for the whole trip as lng,lat pairs and returns one polyline per leg", async () => {
    const { calls, planner } = fakeHttp(200, {
      code: "Ok",
      routes: [{ legs: [{ steps: [step(R, DETOUR), step(DETOUR, A)] }, { steps: [step(A, R)] }] }],
    });
    const legs = await planner.planLegs([R, A, R]);
    expect(calls[0].url).toBe("/route/v1/driving/0,0;0.05,0;0,0");
    expect(calls[0].params).toEqual({ steps: true, geometries: "geojson", overview: "false" });
    expect(legs).toEqual([[R, DETOUR, DETOUR, A], [A, R]]);
    expect(routeFromLegs(legs).path).toEqual([R, DETOUR, A, R]);
  });

  it("rejects when OSRM finds no route or answers with another shape", async () => {
    await expect(fakeHttp(400, { code: "NoRoute", message: "Impossible route" }).planner.planLegs([R, A])).rejects.toThrow(
      /Impossible route/,
    );
    await expect(fakeHttp(200, { code: "Ok", routes: [{ legs: [] }] }).planner.planLegs([R, A])).rejects.toThrow(/legs/);
  });
});

describe("driver movement", () => {
  it("moves by speed times the time actually elapsed", () => {
    const trip = makeTrip();
    const moved = advanceTrip(departTrip(trip, T0), SPEED, T0 + 4 * MINUTE);
    expect(moved.travelledKm).toBeCloseTo(2, 6);
    expect(moved.position.lng).toBeCloseTo(2 / KM_PER_DEG, 6);
    expect(moved.finished).toBe(false);
  });

  it("finishes back at the restaurant and then stays there", () => {
    const trip = makeTrip();
    const done = advanceTrip(departTrip(trip, T0), SPEED, T0 + 10 * 60 * MINUTE);
    expect(done.finished).toBe(true);
    expect(done.travelledKm).toBeCloseTo(trip.totalKm, 6);
    expect(done.position).toEqual(R);
    expect(advanceTrip(done, SPEED, T0 + 20 * 60 * MINUTE)).toBe(done);
  });
});

describe("deliveryProgress", () => {
  it("is WAITING with no driver position before departure", () => {
    const progress = deliveryProgress(makeTrip(), "bB", SPEED);
    expect(progress).toMatchObject({ status: "WAITING", driverPosition: null, etaEpochMs: null, stopsBefore: 1 });
    expect(progress?.remainingKm).toBeCloseTo(0.1 * KM_PER_DEG, 6);
  });

  it("reports distance, eta and the stops still ahead while on the way", () => {
    const trip = makeTrip();
    trip.progress = advanceTrip(departTrip(trip, T0), SPEED, T0 + 4 * MINUTE); // 2 km driven
    const progress = deliveryProgress(trip, "bB", SPEED);
    const remaining = 0.1 * KM_PER_DEG - 2;
    expect(progress?.status).toBe("ON_THE_WAY");
    expect(progress?.remainingKm).toBeCloseTo(remaining, 6);
    expect(progress?.stopsBefore).toBe(1);
    expect(progress?.etaEpochMs).toBe(Math.round(T0 + 4 * MINUTE + (remaining / SPEED) * 3_600_000));
  });

  it("is ARRIVED for a passed stop, pinned to its address, while later stops are still on the way", () => {
    const trip = makeTrip();
    trip.progress = advanceTrip(departTrip(trip, T0), SPEED, T0 + 16 * MINUTE); // 8 km: past A
    expect(deliveryProgress(trip, "bA", SPEED)).toMatchObject({
      status: "ARRIVED",
      driverPosition: A,
      remainingKm: 0,
      etaEpochMs: null,
    });
    expect(deliveryProgress(trip, "bB", SPEED)).toMatchObject({ status: "ON_THE_WAY", stopsBefore: 0 });
  });

  it("is null for a booking that is not a stop of the trip", () => {
    expect(deliveryProgress(makeTrip(), "other", SPEED)).toBeNull();
  });
});

describe("planDeliveryPlacement with a departed trip", () => {
  const limits = { maxDetourKm: 1, maxTripKmBudget: 40, maxTripsPerSlot: 2 };

  it("does not join a trip whose driver has left, and still counts it against the slot", () => {
    const trip = makeTrip();
    const onTheWay: GeoPoint = { lat: 0, lng: 0.07 }; // between A and B: zero detour
    expect(planDeliveryPlacement(R, [trip], onTheWay, limits).kind).toBe("join");

    trip.progress = departTrip(trip, T0);
    expect(planDeliveryPlacement(R, [trip], onTheWay, limits).kind).toBe("new");
    expect(planDeliveryPlacement(R, [trip], onTheWay, { ...limits, maxTripsPerSlot: 1 })).toEqual({
      kind: "rejected",
      reason: "no_trips_left",
    });
  });
});

describe("tracking use cases", () => {
  let bookings: InMemoryBookingRepository;
  let trips: InMemoryTripRepository;
  let now: number;
  let orderStatus: string;
  let statusError: Error | null;
  const clock = () => now;

  const orders: OrderClient = {
    async place(): Promise<PlacedOrder> {
      return { orderId: "o", status: "PENDING" };
    },
    async advance() {},
    async getStatus() {
      if (statusError) throw statusError;
      return orderStatus;
    },
  };

  beforeEach(async () => {
    bookings = new InMemoryBookingRepository();
    trips = new InMemoryTripRepository();
    now = T0;
    orderStatus = "ON_THE_WAY";
    statusError = null;
    planner = new StraightLineRoutePlanner();
    await trips.save(makeTrip());
    await bookings.save(booking("bA", "oA", "alice", A));
    await bookings.save(booking("bB", "oB", "bob", B));
    await bookings.save(booking("bT", "oT", "carol", null));
  });

  let planner: RoutePlanner;
  const advance = (orderId: string) =>
    new AdvanceScheduledOrder(bookings, orders, trips, planner, clock).execute(orderId);
  const tracking = () => new GetOrderTracking(bookings, trips, SPEED);
  const simulator = () => new DriverSimulator(trips, 1000, SPEED, clock);

  it("keeps the driver at the restaurant until an order goes ON_THE_WAY", async () => {
    orderStatus = "READY";
    await advance("oA");
    expect((await trips.findById("t1"))?.progress).toBeNull();
    expect(await simulator().tick()).toEqual({ moved: 0, finished: 0 });
    expect(await tracking().execute("oA")).toMatchObject({ status: "WAITING", driverPosition: null });
  });

  it("departs the trip once, with the first order that goes ON_THE_WAY", async () => {
    await advance("oA");
    now = T0 + 4 * MINUTE;
    await advance("oB");
    expect((await trips.findById("t1"))?.progress?.departedAtEpochMs).toBe(T0);
  });

  it("still advances the order when the status cannot be read back", async () => {
    statusError = new Error("order-service down");
    await expect(advance("oA")).resolves.toMatchObject({ orderId: "oA" });
    expect((await trips.findById("t1"))?.progress).toBeNull();
  });

  it("moves the driver on every tick and shows it through the tracking", async () => {
    await advance("oA");
    now = T0 + 4 * MINUTE;
    expect(await simulator().tick()).toEqual({ moved: 1, finished: 0 });

    const seen = await tracking().execute("oB");
    expect(seen).toMatchObject({ orderId: "oB", tripId: "t1", status: "ON_THE_WAY", origin: R, destination: B });
    expect(seen.speedKmh).toBe(SPEED);
    expect(seen.driverPosition?.lng).toBeCloseTo(2 / KM_PER_DEG, 6);
    expect(seen.updatedAtEpochMs).toBe(now);
  });

  it("stops moving a driver that is back at the restaurant", async () => {
    await advance("oA");
    now = T0 + 10 * 60 * MINUTE;
    expect(await simulator().tick()).toEqual({ moved: 1, finished: 1 });
    expect(await simulator().tick()).toEqual({ moved: 0, finished: 0 });
    expect(await tracking().execute("oB")).toMatchObject({ status: "ARRIVED", driverPosition: B });
  });

  it("on arrival keeps the driver on the road, not on an address that is off it", async () => {
    const KERB: GeoPoint = { lat: 0, lng: 0.049 }; // where the street passes closest to A
    planner = { planLegs: async () => [[R, KERB], [KERB, B], [B, R]] };
    await advance("oA");
    now = T0 + 10 * 60 * MINUTE;
    await simulator().tick();
    const seen = await tracking().execute("oA");
    expect(seen.status).toBe("ARRIVED");
    expect(seen.destination).toEqual(A);
    expect(seen.driverPosition?.lng).toBeCloseTo(KERB.lng, 9);
  });

  it("answers 404 for unknown orders and for orders that are not deliveries", async () => {
    await expect(tracking().execute("nope")).rejects.toMatchObject({ status: 404 });
    await expect(tracking().execute("oT")).rejects.toMatchObject({ status: 404 });
  });

  it("hides another customer's delivery from a customer, but not from a manager", async () => {
    await expect(tracking().execute("oA", { id: "bob", role: "CUSTOMER" })).rejects.toMatchObject({ status: 404 });
    await expect(tracking().execute("oA", { id: "alice", role: "CUSTOMER" })).resolves.toMatchObject({ orderId: "oA" });
    await expect(tracking().execute("oA", { id: "boss", role: "MANAGER" })).resolves.toMatchObject({ orderId: "oA" });
  });

  it("drives the streets the route planner returns, not the straight line", async () => {
    planner = { planLegs: async () => [[R, DETOUR, A], [A, B], [B, R]] };
    await advance("oA");
    const viaDetour = haversineKm(R, DETOUR) + haversineKm(DETOUR, A);
    expect((await tracking().execute("oA")).remainingKm).toBeCloseTo(viaDetour, 6);

    now = T0 + (haversineKm(R, DETOUR) / SPEED) * 60 * MINUTE; // exactly at the corner
    await simulator().tick();
    const seen = await tracking().execute("oA");
    expect(seen.driverPosition?.lat).toBeCloseTo(DETOUR.lat, 6);
    expect(seen.routeAhead?.[seen.routeAhead.length - 1]).toEqual(A);
  });

  it("falls back to straight lines when the route planner fails", async () => {
    planner = {
      planLegs: async () => {
        throw new Error("OSRM down");
      },
    };
    await advance("oA");
    const progress = (await trips.findById("t1"))?.progress;
    expect(progress?.route.path).toEqual([R, A, B, R]);
  });

  it("rejects a non-positive tick or speed", () => {
    expect(() => new DriverSimulator(trips, 0, SPEED)).toThrow();
    expect(() => new DriverSimulator(trips, 1000, 0)).toThrow();
  });
});
