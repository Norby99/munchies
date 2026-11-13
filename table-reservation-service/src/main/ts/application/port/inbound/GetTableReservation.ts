import { ReservationResult } from "./ReservationResult";

/** Use case: reading one reservation. */
export interface GetTableReservation {
  /**
   * @param customerId When given, the reservation must belong to this customer; another
   * customer's reservation is reported as not found, so its existence is not revealed.
   */
  execute(reservationId: string, customerId?: string): Promise<ReservationResult>;
}
