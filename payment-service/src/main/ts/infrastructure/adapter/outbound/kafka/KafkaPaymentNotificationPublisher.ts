import { Producer } from "kafkajs";
import {
  PaymentSuccessNotification,
  PaymentSuccessNotificationInfo,
} from "munchies-payment-service-shared/kotlin/payment-modules";
import { PaymentNotificationPublisher } from "@main/domain/port/payment-notification-publisher";
import { Payment } from "@main/domain/model/Payment";
import getKafka from "./KafkaClient";

/**
 * Kafka adapter publishing payment-success events on the topic notification-service
 * consumes to eventually send user-facing notifications (email, SMS, etc.).
 *
 * The producer connection is established lazily, on first use, and reused for
 * subsequent publications.
 */
export class KafkaPaymentNotificationPublisher implements PaymentNotificationPublisher {
  private readonly topic = PaymentSuccessNotificationInfo.PAYMENT_SUCCESS_TOPIC;
  private producer: Producer | null = null;

  private async getProducer(): Promise<Producer> {
    if (!this.producer) {
      const kafka = await getKafka(this.topic);
      const producer = kafka.producer();
      await producer.connect();
      this.producer = producer;
    }
    return this.producer;
  }

  async publishPaymentSuccess(payment: Payment): Promise<void> {
    const producer = await this.getProducer();

    const notification = new PaymentSuccessNotification(
      payment.id.value,
      payment.orderId.value,
      payment.amount,
      payment.currency
    );

    await producer.send({
      topic: this.topic,
      messages: [{ value: notification.toJson() }],
    });
  }
}
