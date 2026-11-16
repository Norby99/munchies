import { GeoPoint } from "../model/geo";

/** Where the restaurant itself is; both ends of every trip. Null when the restaurant has not set it. */
export interface RestaurantLocation {
  get(restaurantId: string): Promise<GeoPoint | null>;
}
