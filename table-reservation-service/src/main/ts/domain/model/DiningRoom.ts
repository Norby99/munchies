import { InvalidReservationError } from "./InvalidReservationError";

/**
 * A table of a restaurant, as configured by its manager in restaurant-service.
 *
 * @property number The number the staff identifies the table with, unique in the restaurant.
 * @property seats How many guests the table can seat.
 */
export interface Table {
  readonly number: number;
  readonly seats: number;
}

/**
 * The tables of a restaurant, and the rule choosing which one a party is seated at.
 *
 * A party takes exactly one table with enough seats: tables are not joined together. Among the
 * tables that fit, the smallest comes first, so large tables stay free for large parties.
 */
export class DiningRoom {
  private readonly tables: readonly Table[];

  constructor(tables: readonly Table[]) {
    this.tables = [...tables].sort(
      (a, b) => a.seats - b.seats || a.number - b.number,
    );
  }

  /** Whether any table, free or not, is large enough for `numberOfGuests`. */
  canSeat(numberOfGuests: number): boolean {
    return this.tables.some((table) => table.seats >= numberOfGuests);
  }

  /**
   * The tables a party of `numberOfGuests` could take, best choice first.
   *
   * @param takenTableNumbers Tables already booked for the party's time slot.
   * @param preferredTableNumber A table to try before any other when it fits and is free: the
   * one a reservation already holds, so modifying it does not move the party needlessly.
   */
  candidatesFor(
    numberOfGuests: number,
    takenTableNumbers: ReadonlySet<number>,
    preferredTableNumber: number | null = null,
  ): Table[] {
    if (!Number.isInteger(numberOfGuests) || numberOfGuests < 1) {
      throw new InvalidReservationError("A reservation needs at least one guest");
    }
    const free = this.tables.filter(
      (table) => table.seats >= numberOfGuests && !takenTableNumbers.has(table.number),
    );
    const preferred = free.filter((table) => table.number === preferredTableNumber);
    const others = free.filter((table) => table.number !== preferredTableNumber);
    return [...preferred, ...others];
  }
}
