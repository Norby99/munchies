import { Payment } from "@main/domain/model/Payment";

/**
 * Outbound port used to publish payment lifecycle events for downstream
 * consumers. notification-service subscribes to these events to eventually
 * forward user-facing notifications (email, SMS, push, etc.).
 */
export interface PaymentNotificationPublisher {
  publishPaymentSuccess(payment: Payment): Promise<void>;
}
