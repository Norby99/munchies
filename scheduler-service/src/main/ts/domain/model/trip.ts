import { GeoPoint, haversineKm } from "./geo";

/**
 * A virtual driver trip for one restaurant and slot: restaurant -> stops (in order)
 * -> restaurant. Internal accounting only; no driver is assigned here.
 */
export interface TripStop {
  bookingId: string;
  location: GeoPoint;
}

/**
 * The route a departed trip follows, fixed when the driver leaves: restaurant -> stops in
 * order -> restaurant. Never modified afterwards, so it may be shared between copies of a trip.
 */
export interface TripRoute {
  /** The polyline the driver moves along. */
  path: GeoPoint[];
  /** Km along `path` at which each stop is reached; same order as the trip's stops. */
  stopKm: number[];
  /** Length of `path`, km. */
  totalKm: number;
}

/**
 * Where the driver of a departed trip is. Written by the driver simulation (see
 * tripProgress.ts), which stands in for the position a real driver's device would report.
 */
export interface TripProgress {
  departedAtEpochMs: number;
  /** Km driven along the closed route since departure. */
  travelledKm: number;
  /** Last reported driver position. */
  position: GeoPoint;
  updatedAtEpochMs: number;
  /** True once the driver is back at the restaurant: the trip is over. */
  finished: boolean;
  route: TripRoute;
}

export interface Trip {
  id: string;
  restaurantId: string;
  slotStartEpochMs: number;
  /** The restaurant's position when the trip was opened: both ends of the route. */
  origin: GeoPoint;
  stops: TripStop[];
  totalKm: number;
  /** Null until the driver leaves the restaurant. */
  progress: TripProgress | null;
}

export function routeKm(restaurant: GeoPoint, stops: GeoPoint[]): number {
  if (stops.length === 0) return 0;
  let total = haversineKm(restaurant, stops[0]);
  for (let i = 1; i < stops.length; i++) total += haversineKm(stops[i - 1], stops[i]);
  return total + haversineKm(stops[stops.length - 1], restaurant);
}

/** Cheapest position to insert `point` into the route; detour = added km. */
export function cheapestInsertion(
  restaurant: GeoPoint,
  stops: GeoPoint[],
  point: GeoPoint,
): { position: number; detourKm: number } {
  let best = { position: 0, detourKm: Infinity };
  for (let i = 0; i <= stops.length; i++) {
    const prev = i === 0 ? restaurant : stops[i - 1];
    const next = i === stops.length ? restaurant : stops[i];
    const detourKm = Math.max(
      0,
      haversineKm(prev, point) + haversineKm(point, next) - haversineKm(prev, next),
    );
    if (detourKm < best.detourKm) best = { position: i, detourKm };
  }
  return best;
}

export interface PlacementLimits {
  maxDetourKm: number;
  maxTripKmBudget: number;
  maxTripsPerSlot: number;
}

export type TripPlan =
  | { kind: "join"; trip: Trip; position: number; newTotalKm: number }
  | { kind: "new"; costKm: number }
  | { kind: "rejected"; reason: "too_far" | "no_trips_left" };

/**
 * Greedy, append-only insertion. Joins the eligible trip with the smallest detour;
 * otherwise opens a new trip if the slot still has one free. No reshuffling.
 * A trip whose driver has already left cannot be joined, but still counts against
 * maxTripsPerSlot.
 */
export function planDeliveryPlacement(
  restaurant: GeoPoint,
  trips: Trip[],
  point: GeoPoint,
  limits: PlacementLimits,
): TripPlan {
  let best: (TripPlan & { kind: "join" }) | null = null;
  let bestDetour = Infinity;
  for (const trip of trips) {
    if (trip.progress) continue;
    const { position, detourKm } = cheapestInsertion(
      restaurant,
      trip.stops.map((s) => s.location),
      point,
    );
    const newTotalKm = trip.totalKm + detourKm;
    if (detourKm > limits.maxDetourKm || newTotalKm > limits.maxTripKmBudget) continue;
    if (detourKm < bestDetour) {
      bestDetour = detourKm;
      best = { kind: "join", trip, position, newTotalKm };
    }
  }
  if (best) return best;

  const costKm = 2 * haversineKm(restaurant, point);
  if (costKm > limits.maxTripKmBudget) return { kind: "rejected", reason: "too_far" };
  if (trips.length >= limits.maxTripsPerSlot) return { kind: "rejected", reason: "no_trips_left" };
  return { kind: "new", costKm };
}
