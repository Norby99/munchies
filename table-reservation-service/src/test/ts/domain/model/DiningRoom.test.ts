import { describe, expect, it } from "vitest";
import { DiningRoom } from "@main/domain/model/DiningRoom";
import { InvalidReservationError } from "@main/domain/model/InvalidReservationError";

const room = new DiningRoom([
  { number: 4, seats: 6 },
  { number: 1, seats: 2 },
  { number: 3, seats: 4 },
  { number: 2, seats: 4 },
]);
const none = new Set<number>();
const numbers = (tables: { number: number }[]) => tables.map((table) => table.number);

describe("DiningRoom", () => {
  it("offers the tables that fit the party, smallest first, then by number", () => {
    expect(numbers(room.candidatesFor(2, none))).toEqual([1, 2, 3, 4]);
    expect(numbers(room.candidatesFor(3, none))).toEqual([2, 3, 4]);
    expect(numbers(room.candidatesFor(6, none))).toEqual([4]);
  });

  it("leaves out the tables already taken", () => {
    expect(numbers(room.candidatesFor(3, new Set([2])))).toEqual([3, 4]);
    expect(room.candidatesFor(6, new Set([4]))).toEqual([]);
  });

  it("offers the preferred table first when it fits and is free", () => {
    expect(numbers(room.candidatesFor(3, none, 4))).toEqual([4, 2, 3]);
  });

  it("ignores a preferred table that is too small or taken", () => {
    expect(numbers(room.candidatesFor(3, none, 1))).toEqual([2, 3, 4]);
    expect(numbers(room.candidatesFor(3, new Set([3]), 3))).toEqual([2, 4]);
  });

  it("knows whether a party fits at any table at all", () => {
    expect(room.canSeat(6)).toBe(true);
    expect(room.canSeat(7)).toBe(false);
    expect(new DiningRoom([]).canSeat(1)).toBe(false);
  });

  it.each([0, -1, 1.5])("rejects a party of %s guests", (guests) => {
    expect(() => room.candidatesFor(guests, none)).toThrow(InvalidReservationError);
  });
});
