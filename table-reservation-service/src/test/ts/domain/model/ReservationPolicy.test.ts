import { describe, expect, it } from "vitest";
import { InvalidReservationError } from "@main/domain/model/InvalidReservationError";
import { ReservationPolicy } from "@main/domain/model/ReservationPolicy";
import { NOW, at } from "@test/support/fakes";

describe("ReservationPolicy", () => {
  it("books two hours by default", () => {
    const slot = new ReservationPolicy().slotStartingAt(at("19:00"), NOW);
    expect(slot.start).toEqual(at("19:00"));
    expect(slot.end).toEqual(at("21:00"));
  });

  it("books the configured duration", () => {
    const slot = new ReservationPolicy(90).slotStartingAt(at("19:15"), NOW);
    expect(slot.end).toEqual(at("20:45"));
  });

  it.each([0, -30, 100, 22.5, Number.NaN])("rejects a duration of %s minutes", (minutes) => {
    expect(() => new ReservationPolicy(minutes)).toThrow(InvalidReservationError);
  });

  it("refuses a start in the past or right now", () => {
    const policy = new ReservationPolicy();
    expect(() => policy.slotStartingAt(at("09:00"), NOW)).toThrow("in the future");
    expect(() => policy.slotStartingAt(NOW, NOW)).toThrow("in the future");
  });

  it("refuses a start off the 15-minute grid", () => {
    const policy = new ReservationPolicy();
    expect(() => policy.slotStartingAt(at("19:10"), NOW)).toThrow("15-minute boundary");
    expect(() =>
      policy.slotStartingAt(new Date("2026-11-20T19:00:30.000Z"), NOW),
    ).toThrow("15-minute boundary");
  });

  it("refuses a start that is not a date", () => {
    expect(() => new ReservationPolicy().slotStartingAt(new Date("soon"), NOW)).toThrow(
      InvalidReservationError,
    );
  });
});
