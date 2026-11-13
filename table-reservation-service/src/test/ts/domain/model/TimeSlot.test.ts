import { describe, expect, it } from "vitest";
import { InvalidReservationError } from "@main/domain/model/InvalidReservationError";
import { TimeSlot } from "@main/domain/model/TimeSlot";
import { at } from "@test/support/fakes";

describe("TimeSlot", () => {
  it("starts at the given time and lasts the given minutes", () => {
    const slot = TimeSlot.startingAt(at("19:00"), 120);
    expect(slot.start).toEqual(at("19:00"));
    expect(slot.end).toEqual(at("21:00"));
  });

  it("rejects a slot that does not end after it starts", () => {
    expect(() => new TimeSlot(at("19:00"), at("19:00"))).toThrow(InvalidReservationError);
    expect(() => new TimeSlot(at("19:00"), at("18:00"))).toThrow(InvalidReservationError);
  });

  it("rejects an invalid date", () => {
    expect(() => new TimeSlot(new Date("nonsense"), at("19:00"))).toThrow(
      InvalidReservationError,
    );
  });

  it("overlaps a slot sharing some of its time", () => {
    const dinner = TimeSlot.startingAt(at("19:00"), 120);
    expect(dinner.overlaps(TimeSlot.startingAt(at("20:00"), 120))).toBe(true);
    expect(dinner.overlaps(TimeSlot.startingAt(at("18:00"), 120))).toBe(true);
    expect(dinner.overlaps(TimeSlot.startingAt(at("19:30"), 30))).toBe(true);
  });

  it("does not overlap a back-to-back slot", () => {
    const dinner = TimeSlot.startingAt(at("19:00"), 120);
    expect(dinner.overlaps(TimeSlot.startingAt(at("21:00"), 120))).toBe(false);
    expect(dinner.overlaps(TimeSlot.startingAt(at("17:00"), 120))).toBe(false);
  });

  it("has started from its first instant on", () => {
    const slot = TimeSlot.startingAt(at("19:00"), 120);
    expect(slot.hasStartedBy(at("18:45"))).toBe(false);
    expect(slot.hasStartedBy(at("19:00"))).toBe(true);
    expect(slot.hasStartedBy(at("22:00"))).toBe(true);
  });

  it("is not affected by later changes to the dates it was built from", () => {
    const start = at("19:00");
    const slot = TimeSlot.startingAt(start, 120);
    start.setUTCHours(3);
    expect(slot.start).toEqual(at("19:00"));
  });

  it("equals a slot with the same bounds", () => {
    expect(TimeSlot.startingAt(at("19:00"), 60).equals(new TimeSlot(at("19:00"), at("20:00")))).toBe(
      true,
    );
    expect(TimeSlot.startingAt(at("19:00"), 60).equals(TimeSlot.startingAt(at("19:00"), 90))).toBe(
      false,
    );
  });
});
