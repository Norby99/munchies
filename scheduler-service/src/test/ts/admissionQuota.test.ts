import { describe, expect, it } from "vitest";
import { PolicyResolver } from "../../main/ts/application/policyResolver";
import { withAgentQuota } from "../../main/ts/application/agentQuota";
import { CapacityPolicy } from "../../main/ts/domain/model/capacityPolicy";
import { RandomAdmissionQuotaAgent } from "../../main/ts/infrastructure/outbound/randomAdmissionQuotaAgent";

const policy: CapacityPolicy = {
  slotMinutes: 30,
  maxCapacityUnits: 8,
  opening: "11:00",
  closing: "22:00",
  prepLeadMinutes: 30,
  maxAdvanceDays: 7,
  maxTripsPerSlot: 2,
  maxTripKmBudget: 15,
  maxDetourKm: 1,
};

describe("RandomAdmissionQuotaAgent", () => {
  it("draws within [min, max], both ends included", async () => {
    expect(await new RandomAdmissionQuotaAgent(5, 20, () => 0).quotaFor("r")).toBe(5);
    expect(await new RandomAdmissionQuotaAgent(5, 20, () => 0.999999).quotaFor("r")).toBe(20);
  });

  it("keeps the same quota for a restaurant, and draws per restaurant", async () => {
    const draws = [0, 0.999999];
    const agent = new RandomAdmissionQuotaAgent(1, 10, () => draws.shift() ?? 0.5);
    expect(await agent.quotaFor("a")).toBe(1);
    expect(await agent.quotaFor("a")).toBe(1);
    expect(await agent.quotaFor("b")).toBe(10);
  });

  it("rejects an invalid range", () => {
    expect(() => new RandomAdmissionQuotaAgent(0, 5)).toThrow();
    expect(() => new RandomAdmissionQuotaAgent(6, 5)).toThrow();
  });
});

describe("withAgentQuota", () => {
  it("replaces maxCapacityUnits with the agent's quota", async () => {
    const out = await withAgentQuota(policy, "r", { quotaFor: async () => 3 });
    expect(out).toEqual({ ...policy, maxCapacityUnits: 3 });
  });

  it("keeps the policy value without an agent, on failure, or on an invalid quota", async () => {
    expect(await withAgentQuota(policy, "r", undefined)).toBe(policy);
    expect(await withAgentQuota(policy, "r", { quotaFor: async () => { throw new Error("down"); } })).toBe(policy);
    expect(await withAgentQuota(policy, "r", { quotaFor: async () => 0 })).toBe(policy);
    expect(await withAgentQuota(policy, "r", { quotaFor: async () => 2.5 })).toBe(policy);
  });

  it("is applied by PolicyResolver on top of the default policy", async () => {
    const resolver = new PolicyResolver({ getPolicy: async () => null }, policy, { quotaFor: async () => 12 });
    expect((await resolver.resolve("r")).maxCapacityUnits).toBe(12);
  });
});
