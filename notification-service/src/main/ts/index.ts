// Must stay first: it patches kafkajs and pino as they are loaded (see tracing.ts).
import "@main/infrastructure/adapter/outbound/tracing/tracing";
import "@main/domain/external-modules";
import { createServer } from "node:http";
import { Kafka } from "kafkajs";
import getKafka from "./infrastructure/adapter/inbound/kafka/KafkaClient";
import {
  UserEmailConfirmationGroupId,
  UserEmailConfirmationTopic,
  PaymentSuccessNotificationInfo,
  OrderStatusChangedNotificationInfo,
  TableReservationNotificationInfo,
} from "@main/domain/external-modules";
import { KafkaUserEmailConfirmationNotificationConsumer } from "./infrastructure/adapter/inbound/kafka/KafkaUserNotificationConsumer";
import { KafkaPaymentSuccessNotificationConsumer } from "./infrastructure/adapter/inbound/kafka/KafkaPaymentSuccessNotificationConsumer";
import { KafkaOrderStatusChangedNotificationConsumer } from "./infrastructure/adapter/inbound/kafka/KafkaOrderStatusChangedNotificationConsumer";
import { KafkaTableReservationAcceptedNotificationConsumer } from "./infrastructure/adapter/inbound/kafka/KafkaTableReservationAcceptedNotificationConsumer";
import { KafkaTableReservationModifiedNotificationConsumer } from "./infrastructure/adapter/inbound/kafka/KafkaTableReservationModifiedNotificationConsumer";
import { KafkaTableReservationCanceledNotificationConsumer } from "./infrastructure/adapter/inbound/kafka/KafkaTableReservationCanceledNotificationConsumer";
import { NotificationController } from "./infrastructure/adapter/inbound/web/controller/controller";
import { createManagementApp } from "./infrastructure/adapter/inbound/web/config/managementApp";
import { logger } from "./infrastructure/adapter/outbound/logging/logger";
import { SocketIoLiveNotifier } from "./infrastructure/adapter/outbound/websocket/SocketIoLiveNotifier";

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
    logger.error({ err, topic }, "Failed to start consumer");
  }
}

async function main() {
  // One HTTP server carries the management endpoints (health, metrics) and the
  // live-notification WebSocket (Socket.io, under its own path). It listens
  // before the consumers start, so pushes have somewhere to go from the first
  // event on.
  const PORT = process.env.PORT ?? 8080;
  const server = createServer(createManagementApp());
  const notifier = new SocketIoLiveNotifier(server);
  server.listen(PORT, () => {
    logger.info({ port: PORT }, "Management endpoints and live notifications online");
  });

  // Every consumer forwards the events it receives to the same controller,
  // which turns them into live notifications (see NotificationController's docs).
  const controller = new NotificationController(notifier);

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
    startConsumer(
      TableReservationNotificationInfo.TABLE_RESERVATION_ACCEPTED_TOPIC,
      (kafka) =>
        new KafkaTableReservationAcceptedNotificationConsumer(
          kafka,
          TableReservationNotificationInfo.TABLE_RESERVATION_ACCEPTED_TOPIC,
          TableReservationNotificationInfo.TABLE_RESERVATION_ACCEPTED_GROUP_ID,
          controller
        )
    ),
    startConsumer(
      TableReservationNotificationInfo.TABLE_RESERVATION_MODIFIED_TOPIC,
      (kafka) =>
        new KafkaTableReservationModifiedNotificationConsumer(
          kafka,
          TableReservationNotificationInfo.TABLE_RESERVATION_MODIFIED_TOPIC,
          TableReservationNotificationInfo.TABLE_RESERVATION_MODIFIED_GROUP_ID,
          controller
        )
    ),
    startConsumer(
      TableReservationNotificationInfo.TABLE_RESERVATION_CANCELED_TOPIC,
      (kafka) =>
        new KafkaTableReservationCanceledNotificationConsumer(
          kafka,
          TableReservationNotificationInfo.TABLE_RESERVATION_CANCELED_TOPIC,
          TableReservationNotificationInfo.TABLE_RESERVATION_CANCELED_GROUP_ID,
          controller
        )
    ),
  ]);
}

main().catch((err) => {
  logger.error({ err }, "Fatal error during startup");
});
