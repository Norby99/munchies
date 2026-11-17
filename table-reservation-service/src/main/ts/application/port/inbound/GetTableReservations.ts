import { GetTableReservationsCommand } from "./command/GetTableReservationsCommand";
import { ReservationsResult } from "./ReservationResult";

/**
 * Use case: listing reservations, either a customer's own or the ones at a restaurant (for its
 * manager), ordered by the start of their time slot.
 */
export interface GetTableReservations {
  execute(command: GetTableReservationsCommand): Promise<ReservationsResult>;
}
