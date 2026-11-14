import { CapacityPolicy, assertValidPolicy } from "../domain/model/capacityPolicy";
import { RestaurantPolicyClient } from "../domain/ports/restaurantPolicyClient";
import { AdmissionQuotaAgent } from "../domain/ports/admissionQuotaAgent";
import { withAgentQuota } from "./agentQuota";

/**
 * Resolves the capacity policy for a restaurant: the restaurant-service value when
 * present and valid, otherwise the configured default. Never throws for a missing
 * or malformed upstream policy — it degrades to the default and logs.
 *
 * When an admission quota agent is given, its quota replaces the policy's maxCapacityUnits.
 */
export class PolicyResolver {
  constructor(
    private readonly client: RestaurantPolicyClient,
    private readonly defaultPolicy: CapacityPolicy,
    private readonly agent?: AdmissionQuotaAgent,
  ) {
    assertValidPolicy(defaultPolicy);
  }

  async resolve(restaurantId: string): Promise<CapacityPolicy> {
    return withAgentQuota(await this.resolveConfigured(restaurantId), restaurantId, this.agent);
  }

  private async resolveConfigured(restaurantId: string): Promise<CapacityPolicy> {
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
