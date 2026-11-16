import { Post, Route, Tags } from "tsoa";
import { NotificationAPI } from "munchies-notification-service-shared/kotlin/notification-modules";
import {
  _UserEmailConfirmationNotification,
  OrderStatusChangedNotification,
  PaymentSuccessNotification,
  TableReservationAcceptedNotification,
  TableReservationCanceledNotification,
  TableReservationModifiedNotification,
} from "@main/domain/external-modules";
import { notificationsReceived } from "@main/infrastructure/adapter/inbound/web/config/metrics";
import { logger } from "@main/infrastructure/adapter/outbound/logging/logger";
import type { LiveNotifier } from "@main/domain/port/LiveNotifier";
import type {
  LiveNotification,
  ReservationChange,
  ReservationChangedNotification,
} from "@main/domain/model/LiveNotification";

/** The payload shared by the three table-reservation events. */
type TableReservationEvent =
  | TableReservationAcceptedNotification
  | TableReservationModifiedNotification
  | TableReservationCanceledNotification;

/**
 * Central entry point for every notification event the service currently
 * receives, whatever the source or the transport (today, exclusively Kafka,
 * see the kafka consumers under `infrastructure/adapter/inbound/kafka`).
 *
 * This is the "process" step of the agent: each `handle*` method turns one
 * event into the notifications it means for people, and hands them to the
 * {@link LiveNotifier} (the "produce" step, a WebSocket push to the frontend):
 *
 * - order changes go to the order's customer and to its restaurant's staff;
 * - table reservation changes go to the restaurant's staff, and to the
 *   customer too, so their other open tabs and devices stay in step.
 *
 * Email confirmation and payment success are only logged: the first concerns a
 * user who is not signed in yet (its channel would be email), and the second
 * carries no customer or restaurant to route it to; the order change published
 * when the order is marked as paid already updates both sides.
 *
 * Every handler is async and never rejects: the Kafka consumers call them
 * without awaiting (their observer `update` is synchronous), and one failed
 * push must not affect the next message.
 */
@Route("notifications")
@Tags("Notifications")
export class NotificationController extends NotificationAPI {
  constructor(private readonly notifier: LiveNotifier) {
    super();
  }

  /**
   * Creates a notification over HTTP. Currently unused: notification-service
   * only receives events over Kafka, it does not expose a public API.
   */
  @Post()
  public processNotification() {}

  /**
   * Handles a user email-confirmation event, published by user-service once
   * a user successfully verifies their email address.
   */
  public async handleUserEmailConfirmation(
    event: _UserEmailConfirmationNotification
  ): Promise<void> {
    notificationsReceived.inc({ type: "user_email_confirmation" });
    logger.info({ event: event.toString() }, "Received UserEmailConfirmationNotification");
  }

  /**
   * Handles a payment-success event, published by payment-service once a
   * payment completes successfully.
   */
  public async handlePaymentSuccess(event: PaymentSuccessNotification): Promise<void> {
    notificationsReceived.inc({ type: "payment_success" });
    logger.info({ event: event.toString() }, "Received PaymentSuccessNotification");
  }

  /**
   * Handles an order status-change event, published by order-service whenever
   * an order transitions to a new status.
   */
  public async handleOrderStatusChanged(event: OrderStatusChangedNotification): Promise<void> {
    notificationsReceived.inc({ type: "order_status_changed" });
    logger.info({ event: event.toString() }, "Received OrderStatusChangedNotification");
    const notification: LiveNotification = {
      type: "ORDER_CHANGED",
      orderId: event.order_id_key,
      restaurantId: event.restaurant_id_key,
      customerId: event.customer_id_key,
      status: event.status_key,
      payed: event.payed_key,
    };
    await this.push(notification, notification.customerId, notification.restaurantId);
  }

  /**
   * Handles a table-reservation-accepted event, published by table-reservation-service once a table
   * has been booked for a reservation.
   */
  public async handleTableReservationAccepted(
    event: TableReservationAcceptedNotification
  ): Promise<void> {
    notificationsReceived.inc({ type: "table_reservation_accepted" });
    logger.info({ event: event.toString() }, "Received TableReservationAcceptedNotification");
    await this.pushReservation("ACCEPTED", event);
  }

  /**
   * Handles a table-reservation-modified event, published by table-reservation-service once a
   * reservation has changed time slot and/or number of guests.
   */
  public async handleTableReservationModified(
    event: TableReservationModifiedNotification
  ): Promise<void> {
    notificationsReceived.inc({ type: "table_reservation_modified" });
    logger.info({ event: event.toString() }, "Received TableReservationModifiedNotification");
    await this.pushReservation("MODIFIED", event);
  }

  /**
   * Handles a table-reservation-canceled event, published by table-reservation-service once a
   * reservation has been canceled.
   */
  public async handleTableReservationCanceled(
    event: TableReservationCanceledNotification
  ): Promise<void> {
    notificationsReceived.inc({ type: "table_reservation_canceled" });
    logger.info({ event: event.toString() }, "Received TableReservationCanceledNotification");
    await this.pushReservation("CANCELED", event);
  }

  private async pushReservation(change: ReservationChange, event: TableReservationEvent): Promise<void> {
    const notification: ReservationChangedNotification = {
      type: "RESERVATION_CHANGED",
      change,
      reservationId: event.reservationId,
      restaurantId: event.restaurantId,
      customerId: event.customerId,
      start: event.start,
      end: event.end,
      numberOfGuests: event.numberOfGuests,
      tableNumber: event.tableNumber,
    };
    await this.push(notification, notification.customerId, notification.restaurantId);
  }

  /** Pushes to the user and to the restaurant; a failure is logged, never thrown. */
  private async push(notification: LiveNotification, userId: string, restaurantId: string): Promise<void> {
    const results = await Promise.allSettled([
      this.notifier.toUser(userId, notification),
      this.notifier.toRestaurant(restaurantId, notification),
    ]);
    results
      .filter((r): r is PromiseRejectedResult => r.status === "rejected")
      .forEach((r) => logger.error({ err: r.reason, type: notification.type }, "Failed to push a live notification"));
  }
}
