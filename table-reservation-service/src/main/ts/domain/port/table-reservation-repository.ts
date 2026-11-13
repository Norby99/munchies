import { ReservationId } from "@main/domain/model/ReservationId";
import { TableReservation } from "@main/domain/model/TableReservation";
import { TimeSlot } from "@main/domain/model/TimeSlot";

/**
 * Outcome of {@link TableReservationRepository.save}.
 * - SAVED: the reservation is stored.
 * - TABLE_TAKEN: nothing was stored, because another accepted reservation already holds the
 *   same table of the same restaurant in an overlapping time slot.
 */
export type SaveReservationResult = "SAVED" | "TABLE_TAKEN";

/** Which reservations to list; the given fields must all match. */
export interface ReservationFilter {
  customerId?: string;
  restaurantId?: string;
}

/** Outbound port to the store of table reservations. */
export interface TableReservationRepository {
  findById(id: ReservationId): Promise<TableReservation | null>;

  /** The matching reservations, whatever their status, ordered by the start of their slot. */
  find(filter: ReservationFilter): Promise<TableReservation[]>;

  /** The accepted reservations of `restaurantId` sharing any instant with `slot`. */
  findAcceptedOverlapping(restaurantId: string, slot: TimeSlot): Promise<TableReservation[]>;

  /**
   * Stores a new reservation, or replaces the stored one with the same id.
   *
   * This is where double booking is prevented: an implementation must refuse, atomically, an
   * accepted reservation whose table is already held by another accepted reservation in an
   * overlapping slot. Checking with {@link findAcceptedOverlapping} first is not enough, since
   * two requests may check at the same time and both see the table free; callers must be ready
   * for TABLE_TAKEN and try another table.
   */
  save(reservation: TableReservation): Promise<SaveReservationResult>;
}
