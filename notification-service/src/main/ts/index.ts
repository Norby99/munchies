import "@main/domain/external-modules";
import { Kafka } from "kafkajs";
import getKafka from "./infrastructure/adapter/inbound/kafka/KafkaClient";
import {
  UserEmailConfirmationGroupId,
  UserEmailConfirmationTopic,
  PaymentSuccessNotificationInfo,
  OrderStatusChangedNotificationInfo,
} from "@main/domain/external-modules";
import { KafkaUserEmailConfirmationNotificationConsumer } from "./infrastructure/adapter/inbound/kafka/KafkaUserNotificationConsumer";
import { KafkaPaymentSuccessNotificationConsumer } from "./infrastructure/adapter/inbound/kafka/KafkaPaymentSuccessNotificationConsumer";
import { KafkaOrderStatusChangedNotificationConsumer } from "./infrastructure/adapter/inbound/kafka/KafkaOrderStatusChangedNotificationConsumer";
import { NotificationController } from "./infrastructure/adapter/inbound/web/controller/controller";

interface NotificationConsumer {
  connect(): Promise<void>;
  run(): Promise<void>;
}

/**
 * Connects and runs a single topic's consumer in isolation: a startup
 * failure on one topic (e.g. a transient Kafka error) is logged but does
 * not prevent the other topics' consumers from starting.
 */
async function startConsumer(
  topic: string,
  build: (kafka: Kafka) => NotificationConsumer
): Promise<void> {
  try {
    const kafka = await getKafka(topic);
    const consumer = build(kafka);
    await consumer.connect();
    await consumer.run();
  } catch (err) {
    console.error(`[notification-service] Failed to start consumer for topic '${topic}'`, err);
  }
}

async function main() {
  // Every consumer forwards the events it receives to the same controller,
  // which is the single place currently printing notifications instead of
  // dispatching them (see NotificationController's docs).
  const controller = new NotificationController();

  const paymentSuccessTopic = PaymentSuccessNotificationInfo.PAYMENT_SUCCESS_TOPIC;
  const orderStatusChangedTopic =
    OrderStatusChangedNotificationInfo.ORDER_STATUS_CHANGED_TOPIC;

  await Promise.all([
    startConsumer(
      UserEmailConfirmationTopic,
      (kafka) =>
        new KafkaUserEmailConfirmationNotificationConsumer(
          kafka,
          UserEmailConfirmationTopic,
          UserEmailConfirmationGroupId,
          controller
        )
    ),
    startConsumer(
      paymentSuccessTopic,
      (kafka) =>
        new KafkaPaymentSuccessNotificationConsumer(
          kafka,
          paymentSuccessTopic,
          PaymentSuccessNotificationInfo.PAYMENT_SUCCESS_GROUP_ID,
          controller
        )
    ),
    startConsumer(
      orderStatusChangedTopic,
      (kafka) =>
        new KafkaOrderStatusChangedNotificationConsumer(
          kafka,
          orderStatusChangedTopic,
          OrderStatusChangedNotificationInfo.ORDER_STATUS_CHANGED_GROUP_ID,
          controller
        )
    ),
  ]);
}

main().catch((err) => {
  console.error("[notification-service] Fatal error during startup", err);
});
