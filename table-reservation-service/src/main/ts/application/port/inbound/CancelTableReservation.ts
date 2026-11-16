import { ReservationResult } from "./ReservationResult";

/** Use case: a customer cancels their reservation, freeing its table. */
export interface CancelTableReservation {
  /** @param customerId The requester: only the customer who made the reservation may cancel it. */
  execute(reservationId: string, customerId: string): Promise<ReservationResult>;
}
