import { TableReservation } from "@main/domain/model/TableReservation";

/**
 * Outbound port publishing the notification-bound events of a reservation's lifecycle.
 * notification-service subscribes to them to notify the customer and the restaurant.
 *
 * Each event carries the whole reservation (event-carried state transfer), so the consumer
 * never has to call back into this service.
 */
export interface TableReservationNotificationPublisher {
  /** A table has been booked for the reservation. */
  publishAccepted(reservation: TableReservation): Promise<void>;
  /** The reservation changed slot and/or party size; `reservation` is its new state. */
  publishModified(reservation: TableReservation): Promise<void>;
  /** The reservation was canceled; `reservation` still tells what was booked. */
  publishCanceled(reservation: TableReservation): Promise<void>;
}
