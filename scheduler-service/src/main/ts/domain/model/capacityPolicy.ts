/**
 * Per-restaurant capacity configuration. Owned by restaurant-service; the scheduler
 * reads it and falls back to a default when a restaurant has none configured.
 */
export interface CapacityPolicy {
  /** Length of a scheduling slot in minutes. */
  slotMinutes: number;
  /** Kitchen budget per slot: sum of order weights (item quantities) that may be in flight. */
  maxCapacityUnits: number;
  /** First slot start of the day, local "HH:MM". */
  opening: string;
  /** Last moment a slot may end, local "HH:MM". */
  closing: string;
  /** Minutes the kitchen needs between "start preparing" and the slot time. */
  prepLeadMinutes: number;
  /** How many days ahead a customer may schedule. */
  maxAdvanceDays: number;
  /** Driver trips available per slot. */
  maxTripsPerSlot: number;
  /** Maximum closed-route length of one trip (restaurant -> stops -> restaurant), km. */
  maxTripKmBudget: number;
  /** Maximum extra km a delivery may add to an existing trip to join it, km. */
  maxDetourKm: number;
}

const HHMM = /^([01]\d|2[0-3]):([0-5]\d)$/;

export function assertValidPolicy(policy: CapacityPolicy): void {
  const problems: string[] = [];
  const positiveInt = (v: number) => Number.isInteger(v) && v > 0;
  if (!positiveInt(policy.slotMinutes)) problems.push("slotMinutes must be a positive integer");
  if (!positiveInt(policy.maxCapacityUnits)) problems.push("maxCapacityUnits must be a positive integer");
  if (!HHMM.test(policy.opening)) problems.push(`opening must be HH:MM, got "${policy.opening}"`);
  if (!HHMM.test(policy.closing)) problems.push(`closing must be HH:MM, got "${policy.closing}"`);
  if (policy.prepLeadMinutes < 0) problems.push("prepLeadMinutes must be >= 0");
  if (!positiveInt(policy.maxAdvanceDays)) problems.push("maxAdvanceDays must be a positive integer");
  if (!Number.isInteger(policy.maxTripsPerSlot) || policy.maxTripsPerSlot < 0) {
    problems.push("maxTripsPerSlot must be an integer >= 0");
  }
  if (!(policy.maxTripKmBudget > 0)) problems.push("maxTripKmBudget must be > 0");
  if (!(policy.maxDetourKm >= 0)) problems.push("maxDetourKm must be >= 0");
  if (HHMM.test(policy.opening) && HHMM.test(policy.closing) && minutesOfDay(policy.opening) >= minutesOfDay(policy.closing)) {
    problems.push("opening must be before closing");
  }
  if (problems.length > 0) throw new Error(`Invalid capacity policy: ${problems.join("; ")}`);
}

export function minutesOfDay(hhmm: string): number {
  const [h, m] = hhmm.split(":").map(Number);
  return h * 60 + m;
}
