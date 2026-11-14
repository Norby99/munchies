import { ReservationStatus } from "munchies-table-reservation-service-shared/kotlin/table-reservation-modules";
import { ReservationId } from "@main/domain/model/ReservationId";
import { ReservationPolicy } from "@main/domain/model/ReservationPolicy";
import { TableReservation } from "@main/domain/model/TableReservation";
import { TimeSlot } from "@main/domain/model/TimeSlot";
import { TableReservationDocumentData } from "../document/table-reservation-document";

const GRID_STEP_MILLIS = ReservationPolicy.GRANULARITY_MINUTES * 60_000;

/** Mapping between the TableReservation aggregate and its Mongo document. */
export class TableReservationFactory {
  static toDomain(document: TableReservationDocumentData): TableReservation {
    return new TableReservation(
      new ReservationId(document._id),
      document.restaurantId,
      document.customerId,
      new TimeSlot(new Date(document.start), new Date(document.end)),
      Number(document.numberOfGuests),
      ReservationStatus.valueOf(document.status),
      document.tableNumber ?? null,
    );
  }

  static toDocument(domain: TableReservation): TableReservationDocumentData {
    return {
      _id: domain.id.value,
      restaurantId: domain.restaurantId,
      customerId: domain.customerId,
      start: domain.slot.start,
      end: domain.slot.end,
      numberOfGuests: domain.numberOfGuests,
      tableNumber: domain.tableNumber,
      status: domain.status.name,
      occupancy: TableReservationFactory.occupancyOf(domain),
    };
  }

  /**
   * The keys an accepted reservation holds: `<table>@<grid step>` for every step of the
   * arrival-time grid its slot covers. Two reservations of the same table overlap exactly when
   * they share a key, which is what the unique index on the document relies on.
   *
   * The first step is rounded down to the grid, so a slot that were ever off it would still
   * hold every step it touches.
   */
  static occupancyOf(domain: TableReservation): string[] {
    if (!domain.isAccepted || domain.tableNumber === null) return [];
    const keys: string[] = [];
    const first =
      Math.floor(domain.slot.start.getTime() / GRID_STEP_MILLIS) * GRID_STEP_MILLIS;
    for (let step = first; step < domain.slot.end.getTime(); step += GRID_STEP_MILLIS) {
      keys.push(`${domain.tableNumber}@${new Date(step).toISOString()}`);
    }
    return keys;
  }
}
