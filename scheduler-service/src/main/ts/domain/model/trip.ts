import { GeoPoint, haversineKm } from "./geo";

/**
 * A virtual driver trip for one restaurant and slot: restaurant -> stops (in order)
 * -> restaurant. Internal accounting only; no driver is assigned here.
 */
export interface TripStop {
  bookingId: string;
  location: GeoPoint;
}

export interface Trip {
  id: string;
  restaurantId: string;
  slotStartEpochMs: number;
  stops: TripStop[];
  totalKm: number;
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
