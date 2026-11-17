import { CancelTableReservation } from "@main/application/port/inbound/CancelTableReservation";
import { ReservationResult } from "@main/application/port/inbound/ReservationResult";
import { ReservationId } from "@main/domain/model/ReservationId";
import { Logger } from "@main/domain/port/logger";
import { TableReservationNotificationPublisher } from "@main/domain/port/table-reservation-notification-publisher";
import { TableReservationRepository } from "@main/domain/port/table-reservation-repository";

/**
 * Use case canceling a reservation.
 *
 * Only the customer who made the reservation can cancel it, and only while it is accepted and
 * its time has not begun. The canceled reservation is kept (it stays in the customer's
 * history) but no longer holds its table.
 *
 * The canceled event is then published for notification-service, best-effort and not awaited
 * (see ReserveTableUseCase).
 */
export class CancelTableReservationUseCase implements CancelTableReservation {
  constructor(
    private readonly repository: TableReservationRepository,
    private readonly notificationPublisher: TableReservationNotificationPublisher,
    private readonly logger: Logger,
    private readonly now: () => Date = () => new Date(),
  ) {}

  async execute(reservationId: string, customerId: string): Promise<ReservationResult> {
    const current = await this.repository.findById(new ReservationId(reservationId));
    if (!current) {
      return { type: "RESERVATION_NOT_FOUND", reason: "Reservation not found" };
    }
    if (!current.isOwnedBy(customerId)) {
      this.logger.warn("Refused to cancel another customer's reservation", { reservationId });
      return { type: "FORBIDDEN", reason: "You can only cancel your own reservations" };
    }
    if (!current.isAccepted) {
      return { type: "NOT_CHANGEABLE", reason: "The reservation is already canceled" };
    }
    if (current.hasStartedBy(this.now())) {
      return { type: "NOT_CHANGEABLE", reason: "The reservation has already started" };
    }

    const canceled = current.cancel();
    await this.repository.save(canceled);
    this.logger.info("Table reservation canceled", {
      reservationId: canceled.id.value,
      restaurantId: canceled.restaurantId,
    });
    this.notificationPublisher.publishCanceled(canceled).catch((error: unknown) => {
      this.logger.error("Failed to publish the reservation-canceled event", {
        reservationId: canceled.id.value,
        reason: error instanceof Error ? error.message : String(error),
      });
    });
    return { type: "SUCCESS", reservation: canceled };
  }
}
