import { AdmissionQuotaAgent } from "../../domain/ports/admissionQuotaAgent";

/**
 * TEMPORARY stand-in for the CapacityAgent (see the AdmissionQuotaAgent port): picks the
 * admission quota of each restaurant uniformly at random in [min, max], to be replaced by the
 * agent's recommendation.
 *
 * The choice is drawn once per restaurant and kept for the lifetime of the process: a quota
 * that changed on every call would make availability and booking disagree (a slot offered as
 * free could be refused a second later). A restart draws new values.
 */
export class RandomAdmissionQuotaAgent implements AdmissionQuotaAgent {
  private readonly quotas = new Map<string, number>();

  constructor(
    private readonly min: number,
    private readonly max: number,
    private readonly random: () => number = Math.random,
  ) {
    if (!Number.isInteger(min) || !Number.isInteger(max) || min < 1 || max < min) {
      throw new Error(`Invalid admission quota range [${min}, ${max}]: need integers with 1 <= min <= max`);
    }
  }

  async quotaFor(restaurantId: string): Promise<number> {
    let quota = this.quotas.get(restaurantId);
    if (quota === undefined) {
      quota = this.min + Math.floor(this.random() * (this.max - this.min + 1));
      this.quotas.set(restaurantId, quota);
      console.info(`[agent] random admission quota for restaurant ${restaurantId}: ${quota}`);
    }
    return quota;
  }
}
