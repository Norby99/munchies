/**
 * Outbound port to the capacity agent: decides a restaurant's admission quota, i.e. how many
 * kitchen units (sum of item quantities) may be admitted into one scheduling slot. The value
 * replaces `CapacityPolicy.maxCapacityUnits` wherever the scheduler resolves a policy.
 *
 * The intended implementation is the model-based, utility-based `CapacityAgent` of
 * `analysis-scala` (largest quota that keeps the kitchen's delay under its caps). Until that
 * is wired in, `RandomAdmissionQuotaAgent` stands in with a random choice.
 */
export interface AdmissionQuotaAgent {
  quotaFor(restaurantId: string): Promise<number>;
}
