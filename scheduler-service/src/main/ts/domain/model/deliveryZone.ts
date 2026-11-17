import { GeoPoint, isValidGeoPoint } from "./geo";

/**
 * A delivery zone drawn by the restaurant's manager. Owned by restaurant-service; the scheduler
 * only reads it.
 *
 * Distances here are straight lines, so two addresses that look close may still be far apart by
 * road (a river, a railway or a ring road between them). Zones carry the manager's knowledge of
 * the area: when a restaurant has zones, deliveries share a trip only if they all lie in at least
 * one common zone. They narrow batching only; detour and trip budget still apply on top, and an
 * address outside every zone is still delivered, on a trip of its own.
 */
export interface DeliveryZone {
  id: string;
  name: string;
  /** Outer ring of the polygon in drawing order, not closed. No holes. */
  boundary: GeoPoint[];
}

/** Smallest number of vertices that encloses an area. */
const MIN_VERTICES = 3;

export function isValidZone(z: unknown): z is DeliveryZone {
  if (!z || typeof z !== "object") return false;
  const { id, boundary } = z as Record<string, unknown>;
  return (
    typeof id === "string" &&
    id.length > 0 &&
    Array.isArray(boundary) &&
    boundary.length >= MIN_VERTICES &&
    boundary.every(isValidGeoPoint)
  );
}

/**
 * Whether `point` lies inside the polygon (even-odd ray casting). Coordinates are treated as
 * planar (lng as x, lat as y), which is accurate enough at the scale of a city and does not
 * handle polygons crossing the antimeridian. A point exactly on an edge may fall either way.
 */
export function containsPoint(boundary: GeoPoint[], point: GeoPoint): boolean {
  let inside = false;
  for (let i = 0, j = boundary.length - 1; i < boundary.length; j = i++) {
    const a = boundary[i];
    const b = boundary[j];
    const crosses =
      a.lat > point.lat !== b.lat > point.lat &&
      point.lng < ((b.lng - a.lng) * (point.lat - a.lat)) / (b.lat - a.lat) + a.lng;
    if (crosses) inside = !inside;
  }
  return inside;
}

/** Ids of the zones that contain `point`; several when zones overlap, none outside all of them. */
export function zonesContaining(zones: DeliveryZone[], point: GeoPoint): Set<string> {
  const ids = new Set<string>();
  for (const zone of zones) if (containsPoint(zone.boundary, point)) ids.add(zone.id);
  return ids;
}

/**
 * Whether a delivery to `point` may join a trip already serving `stops`. Without zones anything
 * may be batched, as before zones existed. With zones, the stops and the new point must all lie
 * in at least one common zone, so an address outside every zone never shares a trip.
 */
export function mayShareTrip(zones: DeliveryZone[], stops: GeoPoint[], point: GeoPoint): boolean {
  if (zones.length === 0) return true;
  let common = zonesContaining(zones, point);
  for (const stop of stops) {
    if (common.size === 0) return false;
    const stopZones = zonesContaining(zones, stop);
    common = new Set([...common].filter((id) => stopZones.has(id)));
  }
  return common.size > 0;
}
