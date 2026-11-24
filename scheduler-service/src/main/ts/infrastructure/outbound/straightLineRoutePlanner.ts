import { GeoPoint } from "../../domain/model/geo";
import { straightLegs } from "../../domain/model/tripProgress";
import { RoutePlanner } from "../../domain/ports/routePlanner";

/** No routing engine: every leg is the straight line between its two waypoints. */
export class StraightLineRoutePlanner implements RoutePlanner {
  async planLegs(waypoints: GeoPoint[]): Promise<GeoPoint[][]> {
    return straightLegs(waypoints);
  }
}
