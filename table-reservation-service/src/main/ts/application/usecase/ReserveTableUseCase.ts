import { TableBooker } from "@main/application/booking/TableBooker";
import { ReserveTableCommand } from "@main/application/port/inbound/command/ReserveTableCommand";
import { ReservationResult } from "@main/application/port/inbound/ReservationResult";
import { ReserveTable } from "@main/application/port/inbound/ReserveTable";
import { InvalidReservationError } from "@main/domain/model/InvalidReservationError";
import { ReservationPolicy } from "@main/domain/model/ReservationPolicy";
import { TableReservation } from "@main/domain/model/TableReservation";
import { Logger } from "@main/domain/port/logger";
import { TableReservationNotificationPublisher } from "@main/domain/port/table-reservation-notification-publisher";

/**
 * Use case reserving a table.
 *
 * The requested reservation is validated by the domain (a future start on the arrival-time
 * grid, at least one guest), then a table is booked for it through {@link TableBooker}: the
 * reservation is stored as ACCEPTED, or not stored at all when no table is free.
 *
 * Once the reservation is durable, the accepted event is published for notification-service.
 * Publishing is best-effort and not awaited: the customer already has their table, and a slow
 * or unreachable broker must neither delay nor fail the answer.
 */
export class ReserveTableUseCase implements ReserveTable {
  constructor(
    private readonly tableBooker: TableBooker,
    private readonly notificationPublisher: TableReservationNotificationPublisher,
    private readonly logger: Logger,
    private readonly policy: ReservationPolicy = new ReservationPolicy(),
    private readonly now: () => Date = () => new Date(),
  ) {}

  async execute(command: ReserveTableCommand): Promise<ReservationResult> {
    let requested: TableReservation;
    try {
      requested = TableReservation.request(
        command.restaurantId,
        command.customerId,
        this.policy.slotStartingAt(command.start, this.now()),
        command.numberOfGuests,
      );
    } catch (error: unknown) {
      if (error instanceof InvalidReservationError) {
        return { type: "INVALID_REQUEST", reason: error.message };
      }
      throw error;
    }

    const booking = await this.tableBooker.book(
      {
        restaurantId: requested.restaurantId,
        slot: requested.slot,
        numberOfGuests: requested.numberOfGuests,
      },
      (tableNumber) => requested.accept(tableNumber),
    );
    if (booking.type !== "BOOKED") {
      this.logger.warn("Table reservation refused", {
        restaurantId: command.restaurantId,
        outcome: booking.type,
        reason: booking.reason,
      });
      return booking;
    }

    const accepted = booking.reservation;
    this.logger.info("Table reservation accepted", {
      reservationId: accepted.id.value,
      restaurantId: accepted.restaurantId,
      tableNumber: accepted.tableNumber,
      numberOfGuests: accepted.numberOfGuests,
      start: accepted.slot.start.toISOString(),
    });
    this.notificationPublisher.publishAccepted(accepted).catch((error: unknown) => {
      this.logger.error("Failed to publish the reservation-accepted event", {
        reservationId: accepted.id.value,
        reason: error instanceof Error ? error.message : String(error),
      });
    });
    return { type: "SUCCESS", reservation: accepted };
  }
}
