import { ModifyTableReservationCommand } from "./command/ModifyTableReservationCommand";
import { ReservationResult } from "./ReservationResult";

/**
 * Use case: a customer moves their reservation to another time and/or changes the number of
 * guests. A table is booked again for the new slot; on failure the reservation stays as it was.
 */
export interface ModifyTableReservation {
  execute(command: ModifyTableReservationCommand): Promise<ReservationResult>;
}
