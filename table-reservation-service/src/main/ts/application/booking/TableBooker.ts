import { DiningRoom } from "@main/domain/model/DiningRoom";
import { ReservationId } from "@main/domain/model/ReservationId";
import { TableReservation } from "@main/domain/model/TableReservation";
import { TimeSlot } from "@main/domain/model/TimeSlot";
import { RestaurantServiceClient } from "@main/domain/port/restaurant-service-client";
import { TableReservationRepository } from "@main/domain/port/table-reservation-repository";

/** What a table is needed for. */
export interface BookingRequest {
  restaurantId: string;
  slot: TimeSlot;
  numberOfGuests: number;
  /**
   * The reservation being modified, if any: the table it currently holds does not count as
   * taken, and is tried first.
   */
  current?: TableReservation;
}

export type BookingResult =
  | { type: "BOOKED"; reservation: TableReservation }
  | { type: "RESTAURANT_NOT_FOUND"; reason: string }
  | { type: "RESTAURANT_UNAVAILABLE"; reason: string }
  | { type: "NO_TABLE_AVAILABLE"; reason: string };

/**
 * Booking step shared by the use cases that need a table (reserve and modify): reads the
 * restaurant's tables from restaurant-service, picks the ones the party fits at that are free
 * in the slot, and stores the reservation seated at the first one the repository accepts.
 *
 * Reading the free tables and storing the reservation are two separate steps, so a concurrent
 * request may take a table in between. The repository detects it (TABLE_TAKEN) and the next
 * candidate is tried, until one is stored or none is left.
 */
export class TableBooker {
  constructor(
    private readonly repository: TableReservationRepository,
    private readonly restaurantServiceClient: RestaurantServiceClient,
  ) {}

  /**
   * @param seatAt Builds the reservation to store once seated at the given table: an accepted
   * new reservation, or the modified version of an existing one.
   */
  async book(
    request: BookingRequest,
    seatAt: (tableNumber: number) => TableReservation,
  ): Promise<BookingResult> {
    const lookup = await this.restaurantServiceClient.getTables(request.restaurantId);
    if (lookup.type === "NOT_FOUND") {
      return { type: "RESTAURANT_NOT_FOUND", reason: "Restaurant not found" };
    }
    if (lookup.type === "UNAVAILABLE") {
      return { type: "RESTAURANT_UNAVAILABLE", reason: lookup.reason };
    }

    const diningRoom = new DiningRoom(lookup.tables);
    if (!diningRoom.canSeat(request.numberOfGuests)) {
      return {
        type: "NO_TABLE_AVAILABLE",
        reason: `The restaurant has no table for ${request.numberOfGuests} guests`,
      };
    }

    const taken = await this.takenTables(request);
    const candidates = diningRoom.candidatesFor(
      request.numberOfGuests,
      taken,
      request.current?.tableNumber ?? null,
    );
    for (const table of candidates) {
      const reservation = seatAt(table.number);
      if ((await this.repository.save(reservation)) === "SAVED") {
        return { type: "BOOKED", reservation };
      }
    }
    return {
      type: "NO_TABLE_AVAILABLE",
      reason: "No table is free at the requested time",
    };
  }

  /** The tables held, in the requested slot, by reservations other than the current one. */
  private async takenTables(request: BookingRequest): Promise<Set<number>> {
    const overlapping = await this.repository.findAcceptedOverlapping(
      request.restaurantId,
      request.slot,
    );
    const currentId: ReservationId | undefined = request.current?.id;
    const taken = new Set<number>();
    for (const reservation of overlapping) {
      if (reservation.tableNumber === null) continue;
      if (currentId && reservation.id.value === currentId.value) continue;
      taken.add(reservation.tableNumber);
    }
    return taken;
  }
}
