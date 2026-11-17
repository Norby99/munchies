import axios, { AxiosInstance } from "axios";
import { GeoPoint, isValidGeoPoint } from "../../domain/model/geo";
import { RestaurantLocation } from "../../domain/ports/restaurantLocation";

/**
 * Reads the restaurant's own coordinates from restaurant-service.
 *   GET {baseUrl}/restaurants/{restaurantId}/location
 *     200 -> { result: { lat, lng }, code: 200 }
 *     404 -> not set; returns null, which disables deliveries for that restaurant.
 */
export class HttpRestaurantLocation implements RestaurantLocation {
  private readonly http: AxiosInstance;

  constructor(baseUrl: string, http?: AxiosInstance) {
    this.http =
      http ??
      axios.create({
        baseURL: baseUrl,
        timeout: 5_000,
        headers: { "Content-Type": "application/json" },
        validateStatus: () => true,
      });
  }

  async get(restaurantId: string): Promise<GeoPoint | null> {
    const res = await this.http.get(`/restaurants/${encodeURIComponent(restaurantId)}/location`);
    if (res.status === 404) return null;
    if (res.status < 200 || res.status >= 300) {
      throw new Error(`restaurant-service returned HTTP ${res.status} for location`);
    }
    const raw = res.data?.result;
    const point: GeoPoint = { lat: Number(raw?.lat), lng: Number(raw?.lng) };
    return isValidGeoPoint(point) ? point : null;
  }
}
