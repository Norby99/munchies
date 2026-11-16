import { GeoPoint } from "../model/geo";

/**
 * Outbound port to a road-routing engine. Given the points a trip visits in order, it returns
 * one polyline per leg (leg i runs from waypoint i to waypoint i + 1) following the streets.
 * Rejects when no route can be planned; callers then fall back to straight legs.
 */
export interface RoutePlanner {
  planLegs(waypoints: GeoPoint[]): Promise<GeoPoint[][]>;
}
