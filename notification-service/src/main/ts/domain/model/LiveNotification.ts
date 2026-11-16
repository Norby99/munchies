/**
 * A notification pushed live to the users connected to the frontend.
 *
 * Event-carried state transfer, like the Kafka events it comes from: each message carries what
 * the frontend needs to tell the user and to refresh the affected screen, so it never has to
 * ask notification-service for more. The frontend mirrors these types in its `src/types/`
 * (`LiveNotification`); keep both in step.
 */
export type LiveNotification = OrderChangedNotification | ReservationChangedNotification;

/**
 * An order was placed, paid, advanced to its next status or cancelled (order-service publishes
 * all of these as a status change). `status` and `payed` are the order's current ones: a PENDING
 * order not paid yet was just placed, a PENDING paid one was just paid.
 */
export interface OrderChangedNotification {
  type: "ORDER_CHANGED";
  orderId: string;
  restaurantId: string;
  customerId: string;
  status: string;
  payed: boolean;
}

export type ReservationChange = "ACCEPTED" | "MODIFIED" | "CANCELED";

/** A table reservation was booked, changed or canceled. */
export interface ReservationChangedNotification {
  type: "RESERVATION_CHANGED";
  change: ReservationChange;
  reservationId: string;
  restaurantId: string;
  customerId: string;
  /** ISO-8601 instants of the reserved slot. */
  start: string;
  end: string;
  numberOfGuests: number;
  tableNumber: number;
}
