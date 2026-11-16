/**
 * Input of the modify-reservation use case: the reservation's new arrival time and party size
 * (the ones that do not change repeat the current values).
 *
 * @property customerId The requester: only the customer who made the reservation may modify it.
 */
export interface ModifyTableReservationCommand {
  reservationId: string;
  customerId: string;
  start: Date;
  numberOfGuests: number;
}
