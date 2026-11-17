import { GeoPoint, haversineKm } from "./geo";
import { Trip, TripProgress, TripRoute } from "./trip";

/**
 * Driver movement along a trip. There is no real driver device: the position is simulated by
 * moving at a constant speed along the route fixed when the driver leaves (restaurant -> stops
 * in order -> restaurant). The route is a polyline: the streets returned by a RoutePlanner, or
 * straight legs when no road route is available. Everything here is pure; DriverSimulator
 * applies it on a timer.
 */

const MS_PER_HOUR = 3_600_000;

/** The points the route must visit: restaurant, every stop in order, restaurant again. */
export function routeWaypoints(trip: Trip): GeoPoint[] {
  return [trip.origin, ...trip.stops.map((s) => s.location), trip.origin];
}

/** Km from the start of a polyline to its point at `index`. */
export function kmToWaypoint(path: GeoPoint[], index: number): number {
  let km = 0;
  for (let i = 1; i <= index && i < path.length; i++) km += haversineKm(path[i - 1], path[i]);
  return km;
}

/**
 * The point reached after `travelledKm` along a polyline. Linear interpolation inside a
 * segment: accurate enough at the scale of a delivery trip. Clamped to both ends.
 */
export function pointAlongRoute(path: GeoPoint[], travelledKm: number): GeoPoint {
  if (path.length === 0) throw new RangeError("a route needs at least one point");
  let left = Math.max(0, travelledKm);
  for (let i = 1; i < path.length; i++) {
    const from = path[i - 1];
    const to = path[i];
    const legKm = haversineKm(from, to);
    if (left <= legKm) {
      const t = legKm === 0 ? 1 : left / legKm;
      return { lat: from.lat + (to.lat - from.lat) * t, lng: from.lng + (to.lng - from.lng) * t };
    }
    left -= legKm;
  }
  return { ...path[path.length - 1] };
}

/** The part of a polyline between two distances from its start, both ends interpolated. */
export function pathBetween(path: GeoPoint[], fromKm: number, toKm: number): GeoPoint[] {
  if (toKm <= fromKm) return [pointAlongRoute(path, fromKm)];
  const result: GeoPoint[] = [pointAlongRoute(path, fromKm)];
  let km = 0;
  for (let i = 1; i < path.length; i++) {
    km += haversineKm(path[i - 1], path[i]);
    if (km >= toKm) break;
    if (km > fromKm) result.push({ ...path[i] });
  }
  result.push(pointAlongRoute(path, toKm));
  return result;
}

/** One straight leg between each pair of consecutive waypoints: the fallback without roads. */
export function straightLegs(waypoints: GeoPoint[]): GeoPoint[][] {
  const legs: GeoPoint[][] = [];
  for (let i = 1; i < waypoints.length; i++) legs.push([waypoints[i - 1], waypoints[i]]);
  return legs;
}

/**
 * Joins the legs of a trip (leg i runs from waypoint i to waypoint i + 1, so there is one leg
 * more than there are stops) into a single route, remembering how far along it each stop is.
 */
export function routeFromLegs(legs: GeoPoint[][]): TripRoute {
  if (legs.length === 0 || legs.some((leg) => leg.length === 0)) {
    throw new RangeError("a route needs at least one leg and no empty legs");
  }
  const path: GeoPoint[] = [];
  const stopKm: number[] = [];
  let km = 0;
  for (const leg of legs) {
    for (const point of leg) {
      const last = path[path.length - 1];
      if (last) {
        const step = haversineKm(last, point);
        if (step === 0) continue; // legs share their joining point
        km += step;
      }
      path.push({ ...point });
    }
    stopKm.push(km);
  }
  const totalKm = stopKm.pop() as number; // the last leg ends at the restaurant, not at a stop
  return { path, stopKm, totalKm };
}

/** The route of a trip when only straight lines are known. */
export function straightRoute(trip: Trip): TripRoute {
  return routeFromLegs(straightLegs(routeWaypoints(trip)));
}

/** The driver leaves the restaurant along `route`: progress starts with nothing driven. */
export function departTrip(trip: Trip, nowEpochMs: number, route: TripRoute = straightRoute(trip)): TripProgress {
  return {
    departedAtEpochMs: nowEpochMs,
    travelledKm: 0,
    position: { ...route.path[0] },
    updatedAtEpochMs: nowEpochMs,
    finished: false,
    route,
  };
}

/**
 * Moves the driver for the time elapsed since the last report. The elapsed time is measured
 * rather than assumed from the timer period, so a late tick does not slow the driver down.
 */
export function advanceTrip(progress: TripProgress, speedKmh: number, nowEpochMs: number): TripProgress {
  if (progress.finished) return progress;
  const { path, totalKm } = progress.route;
  const elapsedMs = Math.max(0, nowEpochMs - progress.updatedAtEpochMs);
  const travelledKm = Math.min(totalKm, progress.travelledKm + (speedKmh * elapsedMs) / MS_PER_HOUR);
  const finished = travelledKm >= totalKm;
  return {
    ...progress,
    travelledKm,
    // Exactly the end of the route, not an interpolation off by a rounding error.
    position: finished ? { ...path[path.length - 1] } : pointAlongRoute(path, travelledKm),
    updatedAtEpochMs: nowEpochMs,
    finished,
  };
}

export type DeliveryTrackingStatus =
  | "WAITING" // the driver has not left the restaurant yet
  | "ON_THE_WAY"
  | "ARRIVED"; // the driver has reached this delivery's address

/** What one delivery's recipient (or the manager) may know about the trip carrying it. */
export interface DeliveryProgress {
  status: DeliveryTrackingStatus;
  destination: GeoPoint;
  /** Null until the driver leaves. */
  driverPosition: GeoPoint | null;
  /** Km still to drive along the route before reaching this delivery. */
  remainingKm: number;
  /** Null until the driver leaves. */
  etaEpochMs: number | null;
  /** Other deliveries the driver still makes before this one. */
  stopsBefore: number;
  /**
   * The route still to drive, from the driver to this delivery. Null unless on the way.
   * It passes by the earlier stops without marking them.
   */
  routeAhead: GeoPoint[] | null;
  updatedAtEpochMs: number | null;
}

/**
 * Progress of the trip as seen from one of its deliveries, or null when the booking is not a
 * stop of this trip. Deliberately leaves out the other stops' locations: they are other
 * customers' addresses. Before departure the distance is the straight-line estimate, since
 * the road route is only planned when the driver leaves.
 */
export function deliveryProgress(trip: Trip, bookingId: string, speedKmh: number): DeliveryProgress | null {
  const stopIndex = trip.stops.findIndex((s) => s.bookingId === bookingId);
  if (stopIndex < 0) return null;
  const route = trip.progress?.route ?? straightRoute(trip);
  const kmToStop = route.stopKm[stopIndex];
  const travelledKm = trip.progress?.travelledKm ?? 0;
  const remainingKm = Math.max(0, kmToStop - travelledKm);
  const destination = { ...trip.stops[stopIndex].location };
  const stopsBefore = route.stopKm.slice(0, stopIndex).filter((km) => km > travelledKm).length;

  if (!trip.progress) {
    return {
      status: "WAITING",
      destination,
      driverPosition: null,
      remainingKm,
      etaEpochMs: null,
      stopsBefore,
      routeAhead: null,
      updatedAtEpochMs: null,
    };
  }
  const arrived = remainingKm === 0;
  return {
    status: arrived ? "ARRIVED" : "ON_THE_WAY",
    destination,
    // Once delivered, the driver's onward position is no longer this customer's business: it
    // stays where the route reaches this stop. That is the point of the road nearest the
    // address, not the address itself, so the driver is never shown off the street.
    driverPosition: arrived ? pointAlongRoute(route.path, kmToStop) : { ...trip.progress.position },
    remainingKm,
    etaEpochMs: arrived
      ? null
      : Math.round(trip.progress.updatedAtEpochMs + (remainingKm / speedKmh) * MS_PER_HOUR),
    stopsBefore,
    routeAhead: arrived ? null : pathBetween(route.path, travelledKm, kmToStop),
    updatedAtEpochMs: trip.progress.updatedAtEpochMs,
  };
}
