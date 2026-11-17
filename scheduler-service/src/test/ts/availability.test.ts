import { describe, expect, it } from "vitest";
import {
  availableRanges,
  generateSlots,
  isBookableSlot,
  parseDate,
} from "../../main/ts/domain/model/availability";
import { CapacityPolicy } from "../../main/ts/domain/model/capacityPolicy";

const policy: CapacityPolicy = {
  slotMinutes: 30,
  maxCapacityUnits: 2, maxTripsPerSlot: 1, maxTripKmBudget: 10, maxDetourKm: 1,
  opening: "11:00",
  closing: "13:00",
  prepLeadMinutes: 30,
  maxAdvanceDays: 7,
};

describe("parseDate", () => {
  it("rejects malformed dates", () => {
    expect(() => parseDate("2026-13-01")).toThrow(RangeError);
    expect(() => parseDate("nope")).toThrow(RangeError);
    expect(() => parseDate("2026-02-30")).toThrow(RangeError);
  });
});

describe("generateSlots", () => {
  it("produces contiguous slots that fit inside opening/closing", () => {
    const slots = generateSlots("2026-09-01", policy);
    expect(slots.map((s) => `${s.start}-${s.end}`)).toEqual([
      "11:00-11:30",
      "11:30-12:00",
      "12:00-12:30",
      "12:30-13:00",
    ]);
    expect(slots[1].startEpochMs).toBe(new Date(2026, 8, 1, 11, 30).getTime());
  });
});

describe("availableRanges", () => {
  const day = "2026-09-01";
  const slots = generateSlots(day, policy);
  // "now" well before the day so prep-lead and max-advance are satisfied for all slots
  const now = new Date(2026, 8, 1, 8, 0).getTime();

  it("returns every slot with full remaining when nothing is booked", () => {
    const ranges = availableRanges(slots, new Map(), policy, now);
    expect(ranges).toHaveLength(4);
    expect(ranges.every((r) => r.remaining === 2)).toBe(true);
  });

  it("drops a slot once it reaches capacity and decrements the rest", () => {
    const booked = new Map<number, number>([
      [slots[0].startEpochMs, 2],
      [slots[1].startEpochMs, 1],
    ]);
    const ranges = availableRanges(slots, booked, policy, now);
    expect(ranges.map((r) => r.start)).toEqual(["11:30", "12:00", "12:30"]);
    expect(ranges[0].remaining).toBe(1);
  });

  it("hides slots inside the prep-lead window", () => {
    const now2 = new Date(2026, 8, 1, 11, 15).getTime(); // 11:45 is the first prep-feasible start
    const ranges = availableRanges(slots, new Map(), policy, now2);
    expect(ranges.map((r) => r.start)).toEqual(["12:00", "12:30"]);
  });

  it("hides slots beyond the max-advance window", () => {
    const eightDaysBefore = new Date(2026, 7, 24, 8, 0).getTime();
    const ranges = availableRanges(slots, new Map(), policy, eightDaysBefore);
    expect(ranges).toHaveLength(0);
  });
});

describe("isBookableSlot", () => {
  const now = new Date(2026, 8, 1, 8, 0).getTime();

  it("accepts a real, prep-feasible slot", () => {
    const slot = new Date(2026, 8, 1, 12, 0).getTime();
    expect(isBookableSlot(slot, policy, now)).toBe(true);
  });

  it("rejects a timestamp that is not on the slot grid", () => {
    const offGrid = new Date(2026, 8, 1, 12, 7).getTime();
    expect(isBookableSlot(offGrid, policy, now)).toBe(false);
  });

  it("rejects a slot inside the prep-lead window", () => {
    const slot = new Date(2026, 8, 1, 12, 0).getTime();
    const tooLate = new Date(2026, 8, 1, 11, 45).getTime();
    expect(isBookableSlot(slot, policy, tooLate)).toBe(false);
  });
});
