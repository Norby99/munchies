import { CapacityPolicy } from "../model/capacityPolicy";

/**
 * Outbound port for the per-restaurant capacity configuration.
 *   GET {RESTAURANT_SERVICE_URL}/restaurants/{restaurantId}/capacity-policy
 *   200 -> { result: CapacityPolicy, code: 200 }
 *   404 -> not configured; the scheduler uses its default
 */
export interface RestaurantPolicyClient {
  getPolicy(restaurantId: string): Promise<CapacityPolicy | null>;
}
