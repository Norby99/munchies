import { InvalidReservationError } from "./InvalidReservationError";
import { TimeSlot } from "./TimeSlot";

const MILLIS_PER_MINUTE = 60_000;

/**
 * The rules turning the arrival time asked by a customer into the time slot a table is booked
 * for.
 *
 * - Every reservation lasts the same, fixed time (`durationMinutes`): customers choose when
 *   they arrive, not how long they stay.
 * - Arrival times fall on a fixed grid ({@link ReservationPolicy.GRANULARITY_MINUTES}): 19:00,
 *   19:15, 19:30 and so on. The grid is what lets the repository detect two bookings of the
 *   same table atomically (see TableReservationRepository.save), so the duration must be a
 *   multiple of it.
 * - A table can only be reserved for a time yet to come.
 */
export class ReservationPolicy {
  /**
   * Step of the arrival-time grid, in minutes. Deliberately not configurable: stored
   * reservations are indexed by it, changing it would require migrating them.
   */
  static readonly GRANULARITY_MINUTES = 15;

  static readonly DEFAULT_DURATION_MINUTES = 120;

  readonly durationMinutes: number;

  constructor(durationMinutes: number = ReservationPolicy.DEFAULT_DURATION_MINUTES) {
    if (
      !Number.isInteger(durationMinutes) ||
      durationMinutes <= 0 ||
      durationMinutes % ReservationPolicy.GRANULARITY_MINUTES !== 0
    ) {
      throw new InvalidReservationError(
        `The reservation duration must be a positive multiple of ${ReservationPolicy.GRANULARITY_MINUTES} minutes`,
      );
    }
    this.durationMinutes = durationMinutes;
  }

  /**
   * The slot of a reservation whose customer arrives at `start`.
   *
   * @param now The current time, to refuse reservations in the past.
   * @throws InvalidReservationError if `start` is not a date, is not in the future, or is off
   * the arrival-time grid.
   */
  slotStartingAt(start: Date, now: Date): TimeSlot {
    if (Number.isNaN(start.getTime())) {
      throw new InvalidReservationError("The reservation start is not a valid date");
    }
    if (start.getTime() <= now.getTime()) {
      throw new InvalidReservationError("The reservation must start in the future");
    }
    const granularityMillis = ReservationPolicy.GRANULARITY_MINUTES * MILLIS_PER_MINUTE;
    if (start.getTime() % granularityMillis !== 0) {
      throw new InvalidReservationError(
        `The reservation must start on a ${ReservationPolicy.GRANULARITY_MINUTES}-minute boundary`,
      );
    }
    return TimeSlot.startingAt(start, this.durationMinutes);
  }
}
