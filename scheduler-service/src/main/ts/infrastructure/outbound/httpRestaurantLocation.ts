import axios, { AxiosInstance } from "axios";
import { com } from "munchies-restaurant-service-shared";
import { GeoPoint, isValidGeoPoint } from "../../domain/model/geo";
import { RestaurantLocation } from "../../domain/ports/restaurantLocation";

import RestaurantServiceConfig = com.munchies.restaurant.infrastructure.adapter.inbound.web.config.RestaurantServiceConfig;
import getRestaurantLocationResponseFromJson = com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.getRestaurantLocationResponseFromJson;

/**
 * Reads the restaurant's own coordinates from restaurant-service.
 *   GET {SERVICE_PATH}{RESTAURANT_LOCATION_PATH}
 *     200 -> GetRestaurantLocationResponse { result: GeoPointDto { lat, lng }, code: 200 }
 *     404 -> not set; returns null, which disables deliveries for that restaurant.
 * The path and the response parser come from restaurant-shared.
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
    const path = (RestaurantServiceConfig.SERVICE_PATH + RestaurantServiceConfig.RESTAURANT_LOCATION_PATH).replace(
      "{restaurantId}",
      encodeURIComponent(restaurantId),
    );
    // Raw text: the restaurant-shared parser does the mapping.
    const res = await this.http.get<string>(path, {
      responseType: "text",
      transformResponse: (body: unknown) => body,
    });
    if (res.status === 404) return null;
    if (res.status < 200 || res.status >= 300) {
      throw new Error(`restaurant-service returned HTTP ${res.status} for location`);
    }
    let point: GeoPoint;
    try {
      const dto = getRestaurantLocationResponseFromJson(String(res.data)).result;
      point = { lat: dto.lat, lng: dto.lng };
    } catch {
      // A payload that does not match the contract is treated like a missing location, as
      // before: deliveries are disabled for the restaurant rather than failing the request.
      return null;
    }
    return isValidGeoPoint(point) ? point : null;
  }
}
