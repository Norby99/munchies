import { ReserveTableCommand } from "./command/ReserveTableCommand";
import { ReservationResult } from "./ReservationResult";

/**
 * Use case: a customer reserves a table at a restaurant. The request is accepted at once when
 * a table is free for the time slot, and refused otherwise: nothing waits for a manager.
 */
export interface ReserveTable {
  execute(command: ReserveTableCommand): Promise<ReservationResult>;
}
