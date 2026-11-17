import axios, { AxiosInstance } from "axios";
import { DeliveryZone, isValidZone } from "../../domain/model/deliveryZone";
import { RestaurantZones } from "../../domain/ports/restaurantZones";

/**
 * Reads the restaurant's delivery zones from restaurant-service.
 *   GET {baseUrl}/restaurants/{restaurantId}/delivery-zones
 *     200 -> { result: [{ id, name, boundary: [{ lat, lng }, ...] }, ...], code: 200 }
 *     404 -> unknown restaurant; returns no zones (its missing location already disables
 *            deliveries).
 * Any other failure is thrown, like the location lookup: silently ignoring zones would batch
 * deliveries the manager has explicitly kept apart.
 */
export class HttpRestaurantZones implements RestaurantZones {
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

  async get(restaurantId: string): Promise<DeliveryZone[]> {
    const res = await this.http.get(`/restaurants/${encodeURIComponent(restaurantId)}/delivery-zones`);
    if (res.status === 404) return [];
    if (res.status < 200 || res.status >= 300) {
      throw new Error(`restaurant-service returned HTTP ${res.status} for delivery zones`);
    }
    const raw: unknown = res.data?.result;
    if (!Array.isArray(raw)) return [];
    // restaurant-service validates zones on save; a malformed one is dropped rather than failing
    // every delivery of the restaurant.
    return raw.filter(isValidZone).map((z) => ({
      id: z.id,
      name: String(z.name ?? ""),
      boundary: z.boundary.map((p) => ({ lat: p.lat, lng: p.lng })),
    }));
  }
}
