import axios, { AxiosInstance } from "axios";
import { CapacityPolicy } from "../../domain/model/capacityPolicy";
import { RestaurantPolicyClient } from "../../domain/ports/restaurantPolicyClient";

/** Reads the capacity configuration from restaurant-service (see port for the contract). */
export class HttpRestaurantPolicyClient implements RestaurantPolicyClient {
  private readonly http: AxiosInstance;

  constructor(baseUrl: string, http?: AxiosInstance) {
    this.http =
      http ??
      axios.create({
        baseURL: baseUrl,
        timeout: 5_000,
        headers: { "Content-Type": "application/json" },
        validateStatus: () => true,
      });
  }

  async getPolicy(restaurantId: string): Promise<CapacityPolicy | null> {
    const res = await this.http.get(`/restaurants/${encodeURIComponent(restaurantId)}/capacity-policy`);
    if (res.status === 404) return null;
    if (res.status >= 200 && res.status < 300) return this.toPolicy(res.data?.result ?? res.data);
    throw new Error(`restaurant-service returned HTTP ${res.status}`);
  }

  private toPolicy(raw: unknown): CapacityPolicy {
    if (!raw || typeof raw !== "object") throw new Error("capacity policy payload was not an object");
    const r = raw as Record<string, unknown>;
    return {
      slotMinutes: Number(r.slotMinutes),
      maxCapacityUnits: Number(r.maxCapacityUnits),
      opening: String(r.opening),
      closing: String(r.closing),
      prepLeadMinutes: Number(r.prepLeadMinutes),
      maxAdvanceDays: Number(r.maxAdvanceDays),
      maxTripsPerSlot: Number(r.maxTripsPerSlot),
      maxTripKmBudget: Number(r.maxTripKmBudget),
      maxDetourKm: Number(r.maxDetourKm),
    };
  }
}
