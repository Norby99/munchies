import { CapacityPolicy, assertValidPolicy } from "../domain/model/capacityPolicy";
import { RestaurantPolicyClient } from "../domain/ports/restaurantPolicyClient";

/**
 * Resolves the capacity policy for a restaurant: the restaurant-service value when
 * present and valid, otherwise the configured default. Never throws for a missing
 * or malformed upstream policy — it degrades to the default and logs.
 */
export class PolicyResolver {
  constructor(
    private readonly client: RestaurantPolicyClient,
    private readonly defaultPolicy: CapacityPolicy,
  ) {
    assertValidPolicy(defaultPolicy);
  }

  async resolve(restaurantId: string): Promise<CapacityPolicy> {
    let fromService: CapacityPolicy | null = null;
    try {
      fromService = await this.client.getPolicy(restaurantId);
    } catch (err) {
      console.warn(
        `[policy] restaurant-service lookup failed for ${restaurantId}, using default:`,
        err instanceof Error ? err.message : err,
      );
      return this.defaultPolicy;
    }
    if (!fromService) return this.defaultPolicy;
    try {
      assertValidPolicy(fromService);
      return fromService;
    } catch (err) {
      console.warn(
        `[policy] restaurant-service returned an invalid policy for ${restaurantId}, using default:`,
        err instanceof Error ? err.message : err,
      );
      return this.defaultPolicy;
    }
  }
}
