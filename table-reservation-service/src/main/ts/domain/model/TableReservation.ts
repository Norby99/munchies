import { ReservationStatus } from "munchies-table-reservation-service-shared/kotlin/table-reservation-modules";
import { InvalidReservationError } from "./InvalidReservationError";
import { ReservationId } from "./ReservationId";
import { TimeSlot } from "./TimeSlot";

/**
 * Aggregate root of the Table Reservation context: a customer's booking of a table of a
 * restaurant for a time slot.
 *
 * Lifecycle: a reservation is REQUESTED by the customer, becomes ACCEPTED when a table is
 * booked for it, and may later be CANCELED. Only an ACCEPTED reservation holds a table.
 *
 * Instances are immutable: every transition returns the reservation in its new state.
 */
class _TableReservation {
  constructor(
    readonly id: ReservationId,
    readonly restaurantId: string,
    readonly customerId: string,
    readonly slot: TimeSlot,
    readonly numberOfGuests: number,
    readonly status: ReservationStatus,
    /** The table booked for the reservation; `null` until it is accepted. */
    readonly tableNumber: number | null,
  ) {
    if (restaurantId.trim() === "") {
      throw new InvalidReservationError("A reservation needs a restaurant");
    }
    if (customerId.trim() === "") {
      throw new InvalidReservationError("A reservation needs a customer");
    }
    if (!Number.isInteger(numberOfGuests) || numberOfGuests < 1) {
      throw new InvalidReservationError("A reservation needs at least one guest");
    }
    if (status === ReservationStatus.ACCEPTED && tableNumber === null) {
      throw new InvalidReservationError("An accepted reservation needs a table");
    }
  }

  /** A new reservation asked by a customer, with no table yet. */
  static request(
    restaurantId: string,
    customerId: string,
    slot: TimeSlot,
    numberOfGuests: number,
  ): _TableReservation {
    return new _TableReservation(
      new ReservationId(),
      restaurantId,
      customerId,
      slot,
      numberOfGuests,
      ReservationStatus.REQUESTED,
      null,
    );
  }

  /** Books `tableNumber` for a requested reservation. */
  accept(tableNumber: number): _TableReservation {
    if (this.status !== ReservationStatus.REQUESTED) {
      throw new InvalidReservationError("Only a requested reservation can be accepted");
    }
    return this.with({ status: ReservationStatus.ACCEPTED, tableNumber });
  }

  /**
   * Moves an accepted reservation to another slot and/or party size, seated at `tableNumber`
   * (which may be the table it already holds).
   */
  modify(slot: TimeSlot, numberOfGuests: number, tableNumber: number): _TableReservation {
    if (this.status !== ReservationStatus.ACCEPTED) {
      throw new InvalidReservationError("Only an accepted reservation can be modified");
    }
    return this.with({ slot, numberOfGuests, tableNumber });
  }

  /** Withdraws an accepted reservation, freeing its table. */
  cancel(): _TableReservation {
    if (this.status !== ReservationStatus.ACCEPTED) {
      throw new InvalidReservationError("Only an accepted reservation can be canceled");
    }
    return this.with({ status: ReservationStatus.CANCELED });
  }

  get isAccepted(): boolean {
    return this.status === ReservationStatus.ACCEPTED;
  }

  isOwnedBy(customerId: string): boolean {
    return this.customerId === customerId;
  }

  /** Whether the reserved time has begun at `instant`: from then on it cannot change. */
  hasStartedBy(instant: Date): boolean {
    return this.slot.hasStartedBy(instant);
  }

  /** Whether this reservation holds `tableNumber` at any instant of `slot`. */
  occupies(tableNumber: number, slot: TimeSlot): boolean {
    return this.isAccepted && this.tableNumber === tableNumber && this.slot.overlaps(slot);
  }

  private with(
    changes: Partial<
      Pick<_TableReservation, "slot" | "numberOfGuests" | "status" | "tableNumber">
    >,
  ): _TableReservation {
    return new _TableReservation(
      this.id,
      this.restaurantId,
      this.customerId,
      changes.slot ?? this.slot,
      changes.numberOfGuests ?? this.numberOfGuests,
      changes.status ?? this.status,
      changes.tableNumber === undefined ? this.tableNumber : changes.tableNumber,
    );
  }
}

export const TableReservation = _TableReservation;
export type TableReservation = InstanceType<typeof _TableReservation>;
