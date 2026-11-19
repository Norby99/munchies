import axios, { AxiosInstance } from "axios";
import { com } from "munchies-restaurant-service-shared";
import { CapacityPolicy } from "../../domain/model/capacityPolicy";
import { RestaurantPolicyClient } from "../../domain/ports/restaurantPolicyClient";

import RestaurantServiceConfig = com.munchies.restaurant.infrastructure.adapter.inbound.web.config.RestaurantServiceConfig;
import CapacityPolicyDto = com.munchies.restaurant.infrastructure.adapter.dto.CapacityPolicyDto;
import getCapacityPolicyResponseFromJson = com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.getCapacityPolicyResponseFromJson;

/**
 * Reads the capacity configuration from restaurant-service (see the port for the contract):
 *   GET {SERVICE_PATH}{CAPACITY_POLICY_PATH}
 * The path comes from restaurant-shared's `RestaurantServiceConfig` and the body is read with
 * its `GetCapacityPolicyResponse` parser, so this adapter follows the published contract.
 */
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
    const path = (RestaurantServiceConfig.SERVICE_PATH + RestaurantServiceConfig.CAPACITY_POLICY_PATH).replace(
      "{restaurantId}",
      encodeURIComponent(restaurantId),
    );
    // Raw text: the restaurant-shared parser does the mapping.
    const res = await this.http.get<string>(path, {
      responseType: "text",
      transformResponse: (body: unknown) => body,
    });
    if (res.status === 404) return null;
    if (res.status < 200 || res.status >= 300) {
      throw new Error(`restaurant-service returned HTTP ${res.status}`);
    }
    let dto: CapacityPolicyDto;
    try {
      dto = getCapacityPolicyResponseFromJson(String(res.data)).result;
    } catch (err) {
      const reason = err instanceof Error ? err.message : String(err);
      throw new Error(`unexpected capacity policy payload from restaurant-service: ${reason}`);
    }
    return this.toPolicy(dto);
  }

  /** The wire DTO and the domain model carry the same fields; the domain never sees the DTO. */
  private toPolicy(dto: CapacityPolicyDto): CapacityPolicy {
    return {
      slotMinutes: dto.slotMinutes,
      maxCapacityUnits: dto.maxCapacityUnits,
      opening: dto.opening,
      closing: dto.closing,
      prepLeadMinutes: dto.prepLeadMinutes,
      maxAdvanceDays: dto.maxAdvanceDays,
      maxTripsPerSlot: dto.maxTripsPerSlot,
      maxTripKmBudget: dto.maxTripKmBudget,
      maxDetourKm: dto.maxDetourKm,
    };
  }
}
