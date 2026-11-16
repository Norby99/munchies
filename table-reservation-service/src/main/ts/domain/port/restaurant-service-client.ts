import { Table } from "@main/domain/model/DiningRoom";

/**
 * Outcome of reading the tables of a restaurant.
 * - FOUND: the restaurant exists; `tables` is empty when its manager configured none.
 * - NOT_FOUND: no such restaurant.
 * - UNAVAILABLE: restaurant-service could not be asked.
 */
export type TablesLookupResult =
  | { type: "FOUND"; tables: Table[] }
  | { type: "NOT_FOUND" }
  | { type: "UNAVAILABLE"; reason: string };

/** Outcome of asking whether a manager manages a restaurant. */
export type ManagementCheckResult =
  | { type: "MANAGED" }
  | { type: "NOT_MANAGED" }
  | { type: "UNAVAILABLE"; reason: string };

/**
 * Outbound port towards restaurant-service, over its REST API. The Restaurant context owns the
 * tables reservations are booked against, and knows who manages each restaurant.
 */
export interface RestaurantServiceClient {
  getTables(restaurantId: string): Promise<TablesLookupResult>;
  isManagedBy(restaurantId: string, managerId: string): Promise<ManagementCheckResult>;
}
