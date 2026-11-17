import { TableBooker } from "@main/application/booking/TableBooker";
import { ModifyTableReservationCommand } from "@main/application/port/inbound/command/ModifyTableReservationCommand";
import { ModifyTableReservation } from "@main/application/port/inbound/ModifyTableReservation";
import { ReservationResult } from "@main/application/port/inbound/ReservationResult";
import { InvalidReservationError } from "@main/domain/model/InvalidReservationError";
import { ReservationId } from "@main/domain/model/ReservationId";
import { ReservationPolicy } from "@main/domain/model/ReservationPolicy";
import { TimeSlot } from "@main/domain/model/TimeSlot";
import { Logger } from "@main/domain/port/logger";
import { TableReservationNotificationPublisher } from "@main/domain/port/table-reservation-notification-publisher";
import { TableReservationRepository } from "@main/domain/port/table-reservation-repository";

/**
 * Use case modifying a reservation's arrival time and/or number of guests.
 *
 * Only the customer who made the reservation can modify it, and only while it is accepted and
 * its time has not begun. A table is booked again for the new slot through {@link TableBooker},
 * keeping the current table whenever it still fits and is free. The stored reservation is
 * replaced only when a table is found, so a refused change leaves it untouched.
 *
 * The modified event is then published for notification-service, best-effort and not awaited
 * (see ReserveTableUseCase).
 */
export class ModifyTableReservationUseCase implements ModifyTableReservation {
  constructor(
    private readonly repository: TableReservationRepository,
    private readonly tableBooker: TableBooker,
    private readonly notificationPublisher: TableReservationNotificationPublisher,
    private readonly logger: Logger,
    private readonly policy: ReservationPolicy = new ReservationPolicy(),
    private readonly now: () => Date = () => new Date(),
  ) {}

  async execute(command: ModifyTableReservationCommand): Promise<ReservationResult> {
    const current = await this.repository.findById(new ReservationId(command.reservationId));
    if (!current) {
      return { type: "RESERVATION_NOT_FOUND", reason: "Reservation not found" };
    }
    if (!current.isOwnedBy(command.customerId)) {
      this.logger.warn("Refused to modify another customer's reservation", {
        reservationId: command.reservationId,
      });
      return { type: "FORBIDDEN", reason: "You can only modify your own reservations" };
    }
    const now = this.now();
    if (!current.isAccepted) {
      return { type: "NOT_CHANGEABLE", reason: "The reservation is canceled" };
    }
    if (current.hasStartedBy(now)) {
      return { type: "NOT_CHANGEABLE", reason: "The reservation has already started" };
    }

    let slot: TimeSlot;
    try {
      slot = this.policy.slotStartingAt(command.start, now);
      // Validates the party size before any table is looked for.
      current.modify(slot, command.numberOfGuests, current.tableNumber!);
    } catch (error: unknown) {
      if (error instanceof InvalidReservationError) {
        return { type: "INVALID_REQUEST", reason: error.message };
      }
      throw error;
    }

    const booking = await this.tableBooker.book(
      {
        restaurantId: current.restaurantId,
        slot,
        numberOfGuests: command.numberOfGuests,
        current,
      },
      (tableNumber) => current.modify(slot, command.numberOfGuests, tableNumber),
    );
    if (booking.type !== "BOOKED") {
      this.logger.warn("Table reservation change refused", {
        reservationId: command.reservationId,
        outcome: booking.type,
        reason: booking.reason,
      });
      return booking;
    }

    const modified = booking.reservation;
    this.logger.info("Table reservation modified", {
      reservationId: modified.id.value,
      tableNumber: modified.tableNumber,
      numberOfGuests: modified.numberOfGuests,
      start: modified.slot.start.toISOString(),
    });
    this.notificationPublisher.publishModified(modified).catch((error: unknown) => {
      this.logger.error("Failed to publish the reservation-modified event", {
        reservationId: modified.id.value,
        reason: error instanceof Error ? error.message : String(error),
      });
    });
    return { type: "SUCCESS", reservation: modified };
  }
}
