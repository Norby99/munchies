import { InvalidReservationError } from "./InvalidReservationError";

const MILLIS_PER_MINUTE = 60_000;

/**
 * The time a table is reserved for: the half-open interval `[start, end)`.
 *
 * Half-open so that back-to-back reservations do not collide: a slot ending at 21:00 and one
 * starting at 21:00 can share the same table.
 */
export class TimeSlot {
  readonly start: Date;
  readonly end: Date;

  constructor(start: Date, end: Date) {
    if (Number.isNaN(start.getTime()) || Number.isNaN(end.getTime())) {
      throw new InvalidReservationError("The time slot is not a valid date");
    }
    if (end.getTime() <= start.getTime()) {
      throw new InvalidReservationError("The time slot must end after it starts");
    }
    // Copies: a Date is mutable, the slot must not change behind the reservation's back.
    this.start = new Date(start.getTime());
    this.end = new Date(end.getTime());
  }

  /** The slot starting at `start` and lasting `durationMinutes`. */
  static startingAt(start: Date, durationMinutes: number): TimeSlot {
    return new TimeSlot(
      start,
      new Date(start.getTime() + durationMinutes * MILLIS_PER_MINUTE),
    );
  }

  /** Whether the two slots share any instant. */
  overlaps(other: TimeSlot): boolean {
    return (
      this.start.getTime() < other.end.getTime() &&
      other.start.getTime() < this.end.getTime()
    );
  }

  /** Whether the slot has begun (or is over) at `instant`. */
  hasStartedBy(instant: Date): boolean {
    return this.start.getTime() <= instant.getTime();
  }

  equals(other: TimeSlot): boolean {
    return (
      this.start.getTime() === other.start.getTime() &&
      this.end.getTime() === other.end.getTime()
    );
  }
}
