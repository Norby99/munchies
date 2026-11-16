import { CapacityPolicy } from "../domain/model/capacityPolicy";
import { AdmissionQuotaAgent } from "../domain/ports/admissionQuotaAgent";

/**
 * Applies the agent's admission quota to a resolved policy (as its maxCapacityUnits). Without an
 * agent, or if the agent fails or answers something that is not a positive integer, the policy
 * is returned unchanged: the agent tunes capacity, it must never take scheduling down.
 */
export async function withAgentQuota(
  policy: CapacityPolicy,
  restaurantId: string,
  agent: AdmissionQuotaAgent | undefined,
): Promise<CapacityPolicy> {
  if (!agent) return policy;
  try {
    const quota = await agent.quotaFor(restaurantId);
    if (Number.isInteger(quota) && quota > 0) return { ...policy, maxCapacityUnits: quota };
    console.warn(`[agent] ignoring invalid admission quota ${quota} for restaurant ${restaurantId}`);
  } catch (err) {
    console.warn(
      `[agent] admission quota lookup failed for ${restaurantId}, keeping the policy value:`,
      err instanceof Error ? err.message : err,
    );
  }
  return policy;
}
