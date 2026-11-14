import axios from "axios";
import {
  RestaurantServiceConfig,
  getManagerRestaurantsResponseFromJson,
  getTablesResponseFromJson,
} from "munchies-restaurant-service-shared/kotlin/restaurant-modules";
import {
  ManagementCheckResult,
  RestaurantServiceClient,
  TablesLookupResult,
} from "@main/domain/port/restaurant-service-client";

/** A customer is waiting on the reservation: a slow restaurant-service must not hold it. */
const TIMEOUT_MILLIS = 3_000;

/**
 * REST adapter towards restaurant-service.
 *
 * It reads a restaurant's tables with `GET {SERVICE_PATH}{TABLES_PATH}` and a manager's
 * restaurants with `GET {SERVICE_PATH}{GET_MANAGER_RESTAURANTS_PATH}`. Paths and response
 * parsers are reused from restaurant-shared, so this adapter cannot drift from the real
 * contract. Every transport, status or decoding failure becomes UNAVAILABLE: the caller cannot
 * decide and must not guess.
 */
export class RestaurantServiceHttpClient implements RestaurantServiceClient {
  async getTables(restaurantId: string): Promise<TablesLookupResult> {
    const path = (
      RestaurantServiceConfig.SERVICE_PATH + RestaurantServiceConfig.TABLES_PATH
    ).replace("{restaurantId}", encodeURIComponent(restaurantId));

    const response = await this.get(path);
    if (response.type === "UNAVAILABLE") return response;
    if (response.status === 404) return { type: "NOT_FOUND" };
    if (response.status >= 400) return unexpectedStatus(response.status);
    try {
      const tables = getTablesResponseFromJson(response.body).result;
      return {
        type: "FOUND",
        tables: Array.from(tables, (table) => ({ number: table.number, seats: table.seats })),
      };
    } catch (error: unknown) {
      return undecodable(error);
    }
  }

  async isManagedBy(restaurantId: string, managerId: string): Promise<ManagementCheckResult> {
    const path = (
      RestaurantServiceConfig.SERVICE_PATH +
      RestaurantServiceConfig.GET_MANAGER_RESTAURANTS_PATH
    ).replace("{managerId}", encodeURIComponent(managerId));

    const response = await this.get(path);
    if (response.type === "UNAVAILABLE") return response;
    if (response.status >= 400) return unexpectedStatus(response.status);
    try {
      const restaurants = getManagerRestaurantsResponseFromJson(response.body).result;
      const managed = Array.from(restaurants).some(
        (restaurant) => restaurant.id === restaurantId,
      );
      return { type: managed ? "MANAGED" : "NOT_MANAGED" };
    } catch (error: unknown) {
      return undecodable(error);
    }
  }

  private async get(
    path: string,
  ): Promise<
    { type: "ANSWERED"; status: number; body: string } | { type: "UNAVAILABLE"; reason: string }
  > {
    const baseUrl = process.env.RESTAURANT_SERVICE_URL;
    if (!baseUrl) {
      return { type: "UNAVAILABLE", reason: "Missing Restaurant Service URL" };
    }
    try {
      // Raw text: the shared parsers do the mapping, as for every other contract.
      const response = await axios.get<string>(baseUrl + path, {
        responseType: "text",
        transformResponse: (body) => body,
        validateStatus: () => true,
        timeout: TIMEOUT_MILLIS,
      });
      return { type: "ANSWERED", status: response.status, body: String(response.data) };
    } catch (error: unknown) {
      const message = error instanceof Error ? error.message : String(error);
      return { type: "UNAVAILABLE", reason: "Restaurant Service request failed: " + message };
    }
  }
}

function unexpectedStatus(status: number): { type: "UNAVAILABLE"; reason: string } {
  return { type: "UNAVAILABLE", reason: `Restaurant Service responded with status ${status}` };
}

function undecodable(error: unknown): { type: "UNAVAILABLE"; reason: string } {
  const message = error instanceof Error ? error.message : String(error);
  return { type: "UNAVAILABLE", reason: "Restaurant Service answer not understood: " + message };
}
