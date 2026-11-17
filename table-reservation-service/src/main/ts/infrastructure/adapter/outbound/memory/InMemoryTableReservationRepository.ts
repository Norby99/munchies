import { ReservationId } from "@main/domain/model/ReservationId";
import { TableReservation } from "@main/domain/model/TableReservation";
import { TimeSlot } from "@main/domain/model/TimeSlot";
import {
  ReservationFilter,
  SaveReservationResult,
  TableReservationRepository,
} from "@main/domain/port/table-reservation-repository";

/**
 * In-memory reservation store, for running the service without MongoDB (local runs, tests).
 * Reservations are lost on restart and not shared between replicas.
 */
export class InMemoryTableReservationRepository implements TableReservationRepository {
  private readonly reservations = new Map<string, TableReservation>();

  findById(id: ReservationId): Promise<TableReservation | null> {
    return Promise.resolve(this.reservations.get(id.value) ?? null);
  }

  find(filter: ReservationFilter): Promise<TableReservation[]> {
    const matching = [...this.reservations.values()]
      .filter(
        (reservation) =>
          (!filter.customerId || reservation.customerId === filter.customerId) &&
          (!filter.restaurantId || reservation.restaurantId === filter.restaurantId),
      )
      .sort((a, b) => a.slot.start.getTime() - b.slot.start.getTime());
    return Promise.resolve(matching);
  }

  findAcceptedOverlapping(restaurantId: string, slot: TimeSlot): Promise<TableReservation[]> {
    return Promise.resolve(
      [...this.reservations.values()].filter(
        (reservation) =>
          reservation.restaurantId === restaurantId &&
          reservation.isAccepted &&
          reservation.slot.overlaps(slot),
      ),
    );
  }

  /**
   * The check and the write happen in the same synchronous step, so no other request can slip
   * in between: this is atomic within the process, as the port requires.
   */
  save(reservation: TableReservation): Promise<SaveReservationResult> {
    if (reservation.isAccepted && reservation.tableNumber !== null) {
      const tableNumber = reservation.tableNumber;
      const taken = [...this.reservations.values()].some(
        (other) =>
          other.id.value !== reservation.id.value &&
          other.restaurantId === reservation.restaurantId &&
          other.occupies(tableNumber, reservation.slot),
      );
      if (taken) return Promise.resolve("TABLE_TAKEN");
    }
    this.reservations.set(reservation.id.value, reservation);
    return Promise.resolve("SAVED");
  }
}
