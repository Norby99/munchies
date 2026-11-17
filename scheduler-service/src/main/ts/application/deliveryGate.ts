import { CapacityPolicy, assertValidPolicy } from "../domain/model/capacityPolicy";
import { GeoPoint } from "../domain/model/geo";
import { RestaurantLocation } from "../domain/ports/restaurantLocation";
import { RestaurantPolicyClient } from "../domain/ports/restaurantPolicyClient";
import { AdmissionQuotaAgent } from "../domain/ports/admissionQuotaAgent";
import { withAgentQuota } from "./agentQuota";

export interface DeliverySetup {
  policy: CapacityPolicy;
  origin: GeoPoint;
}

/**
 * Deliveries are offered only once the restaurant has set both its location and its own
 * capacity policy in restaurant-service. Nothing is defaulted here, unlike PolicyResolver:
 * a default trip budget or a default origin would quote deliveries the restaurant never
 * agreed to serve. Returns null while either is missing or invalid.
 *
 * When an admission quota agent is given, its quota replaces the policy's maxCapacityUnits.
 */
export class DeliveryGate {
  constructor(
    private readonly policies: RestaurantPolicyClient,
    private readonly locations: RestaurantLocation,
    private readonly agent?: AdmissionQuotaAgent,
  ) {}

  async resolve(restaurantId: string): Promise<DeliverySetup | null> {
    const [policy, origin] = await Promise.all([
      this.policies.getPolicy(restaurantId),
      this.locations.get(restaurantId),
    ]);
    if (!policy || !origin) return null;
    try {
      assertValidPolicy(policy);
    } catch {
      return null;
    }
    return { policy: await withAgentQuota(policy, restaurantId, this.agent), origin };
  }
}
