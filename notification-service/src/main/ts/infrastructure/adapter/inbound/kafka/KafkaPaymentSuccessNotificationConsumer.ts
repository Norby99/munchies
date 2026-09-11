import {
  PaymentSuccessNotification,
  PaymentSuccessNotificationObserver,
  paymentSuccessNotificationFromJson,
} from "@main/domain/external-modules";
import { NotificationController } from "@main/infrastructure/adapter/inbound/web/controller/controller";
import { Kafka, Consumer } from "kafkajs";

/**
 * Kafka consumer for the payment-success topic. Parses incoming messages
 * and forwards them to the shared {@link NotificationController}.
 */
export class KafkaPaymentSuccessNotificationConsumer extends PaymentSuccessNotificationObserver {
  private consumer: Consumer;
  private topic: string;

  constructor(
    kafka: Kafka,
    topic: string,
    groupId: string,
    private readonly controller: NotificationController
  ) {
    super();
    this.consumer = kafka.consumer({ groupId });
    this.topic = topic;
  }

  async connect() {
    this.consumer.connect();
    await this.consumer.subscribe({ topic: this.topic, fromBeginning: true });
  }

  async disconnect() {
    await this.consumer.disconnect();
  }

  async run() {
    await this.consumer.run({
      eachMessage: async ({ message }) => {
        if (message.value) {
          try {
            const event = paymentSuccessNotificationFromJson(
              message.value.toString()
            );
            this.update(event);
          } catch (err) {
            console.error("Failed to parse PaymentSuccessNotification message", err);
          }
        }
      },
    });
  }

  override update(event: PaymentSuccessNotification): void {
    this.controller.handlePaymentSuccess(event);
  }
}
