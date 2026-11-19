import axios, { AxiosInstance } from "axios";
import { com } from "munchies-restaurant-service-shared";
import { DeliveryZone, isValidZone } from "../../domain/model/deliveryZone";
import { RestaurantZones } from "../../domain/ports/restaurantZones";

import RestaurantServiceConfig = com.munchies.restaurant.infrastructure.adapter.inbound.web.config.RestaurantServiceConfig;
import getDeliveryZonesResponseFromJson = com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.getDeliveryZonesResponseFromJson;

/**
 * Reads the restaurant's delivery zones from restaurant-service.
 *   GET {SERVICE_PATH}{DELIVERY_ZONES_PATH}
 *     200 -> GetDeliveryZonesResponse { result: [DeliveryZoneDto { id, name, boundary: [{ lat, lng }] }], code: 200 }
 *     404 -> unknown restaurant; returns no zones (its missing location already disables
 *            deliveries).
 * The path and the response parser come from restaurant-shared.
 *
 * Any other failure is thrown, like the location lookup, and so is a payload that does not match
 * the contract: silently ignoring zones would batch deliveries the manager has explicitly kept
 * apart.
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
    const path = (RestaurantServiceConfig.SERVICE_PATH + RestaurantServiceConfig.DELIVERY_ZONES_PATH).replace(
      "{restaurantId}",
      encodeURIComponent(restaurantId),
    );
    // Raw text: the restaurant-shared parser does the mapping.
    const res = await this.http.get<string>(path, {
      responseType: "text",
      transformResponse: (body: unknown) => body,
    });
    if (res.status === 404) return [];
    if (res.status < 200 || res.status >= 300) {
      throw new Error(`restaurant-service returned HTTP ${res.status} for delivery zones`);
    }
    let zones: DeliveryZone[];
    try {
      zones = Array.from(getDeliveryZonesResponseFromJson(String(res.data)).result, (z) => ({
        id: z.id,
        name: z.name,
        boundary: Array.from(z.boundary, (p) => ({ lat: p.lat, lng: p.lng })),
      }));
    } catch {
      throw new Error("restaurant-service returned delivery zones that do not match the contract");
    }
    // restaurant-service validates zones on save; a zone that is still unusable (fewer than three
    // vertices, coordinates out of range) is dropped rather than failing every delivery of the
    // restaurant.
    return zones.filter(isValidZone);
  }
}
