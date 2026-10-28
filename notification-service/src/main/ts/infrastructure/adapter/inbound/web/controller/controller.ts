import { Post, Route, Tags } from "tsoa";
import { NotificationAPI } from "munchies-notification-service-shared/kotlin/notification-modules";
import {
  _UserEmailConfirmationNotification,
  OrderStatusChangedNotification,
  PaymentSuccessNotification,
} from "@main/domain/external-modules";
import { notificationsReceived } from "@main/infrastructure/adapter/inbound/web/config/metrics";
import { logger } from "@main/infrastructure/adapter/outbound/logging/logger";

/**
 * Central entry point for every notification event the service currently
 * receives, whatever the source or the transport (today, exclusively Kafka,
 * see the kafka consumers under `infrastructure/adapter/inbound/kafka`).
 *
 * Handling a notification is, for now, a deliberate stub: it only logs the
 * event to the console. Once real notification channels (email, SMS, push,
 * etc.) are implemented, or mocked for testing, each `handle*` method below
 * is the single place where that dispatch logic should be plugged in, one
 * notification type at a time.
 */
@Route("notifications")
@Tags("Notifications")
export class NotificationController extends NotificationAPI {
  constructor() {
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
  public handleUserEmailConfirmation(
    event: _UserEmailConfirmationNotification
  ): void {
    notificationsReceived.inc({ type: "user_email_confirmation" });
    logger.info({ event: event.toString() }, "Received UserEmailConfirmationNotification");
  }

  /**
   * Handles a payment-success event, published by payment-service once a
   * payment completes successfully.
   */
  public handlePaymentSuccess(event: PaymentSuccessNotification): void {
    notificationsReceived.inc({ type: "payment_success" });
    logger.info({ event: event.toString() }, "Received PaymentSuccessNotification");
  }

  /**
   * Handles an order status-change event, published by order-service whenever
   * an order transitions to a new status.
   */
  public handleOrderStatusChanged(event: OrderStatusChangedNotification): void {
    logger.info({ event: event.toString() }, "Received OrderStatusChangedNotification");
  }
}
