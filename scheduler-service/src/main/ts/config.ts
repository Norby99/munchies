import type { CapacityPolicy } from "./domain/model/capacityPolicy";

/** Runtime configuration, resolved once from the environment at process start. */
export interface SchedulerConfig {
  port: number;
  orderServiceUrl: string;
  restaurantServiceUrl: string;
  transitionTickMs: number;
  defaultPolicy: CapacityPolicy;
  /** Range of the TEMPORARY random admission quota (see RandomAdmissionQuotaAgent). */
  admissionQuotaMin: number;
  admissionQuotaMax: number;
}

function num(name: string, fallback: number): number {
  const raw = process.env[name];
  if (raw === undefined || raw === "") return fallback;
  const parsed = Number(raw);
  if (!Number.isFinite(parsed)) throw new Error(`Environment variable ${name} is not a number: "${raw}"`);
  return parsed;
}

function str(name: string, fallback: string): string {
  const raw = process.env[name];
  return raw === undefined || raw === "" ? fallback : raw;
}

export function loadConfig(env: NodeJS.ProcessEnv = process.env): SchedulerConfig {
  void env;
  return {
    port: num("PORT", 8080),
    orderServiceUrl: str("ORDER_SERVICE_URL", "http://order-service:8080").replace(/\/+$/, ""),
    restaurantServiceUrl: str("RESTAURANT_SERVICE_URL", "http://restaurant-service:8080").replace(/\/+$/, ""),
    transitionTickMs: num("TRANSITION_TICK_MS", 30_000),
    admissionQuotaMin: num("ADMISSION_QUOTA_MIN", 5),
    admissionQuotaMax: num("ADMISSION_QUOTA_MAX", 20),
    defaultPolicy: {
      slotMinutes: num("DEFAULT_SLOT_MINUTES", 30),
      maxCapacityUnits: num("DEFAULT_MAX_CAPACITY_UNITS", 8),
      opening: str("DEFAULT_OPENING", "11:00"),
      closing: str("DEFAULT_CLOSING", "22:00"),
      prepLeadMinutes: num("DEFAULT_PREP_LEAD_MINUTES", 30),
      maxAdvanceDays: num("DEFAULT_MAX_ADVANCE_DAYS", 7),
      maxTripsPerSlot: num("DEFAULT_MAX_TRIPS_PER_SLOT", 2),
      maxTripKmBudget: num("DEFAULT_MAX_TRIP_KM_BUDGET", 15),
      maxDetourKm: num("DEFAULT_MAX_DETOUR_KM", 1),
    },
  };
}
