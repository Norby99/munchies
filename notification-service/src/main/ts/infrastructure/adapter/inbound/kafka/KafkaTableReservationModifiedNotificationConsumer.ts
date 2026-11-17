import {
  TableReservationModifiedNotification,
  TableReservationModifiedNotificationObserver,
  tableReservationModifiedNotificationFromJson,
} from "@main/domain/external-modules";
import { NotificationController } from "@main/infrastructure/adapter/inbound/web/controller/controller";
import { Kafka, Consumer } from "kafkajs";
import { logger } from "@main/infrastructure/adapter/outbound/logging/logger";

/**
 * Kafka consumer for the table-reservation-modified topic. Parses incoming messages
 * and forwards them to the shared {@link NotificationController}.
 */
export class KafkaTableReservationModifiedNotificationConsumer extends TableReservationModifiedNotificationObserver {
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
    await this.consumer.connect();
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
            const event = tableReservationModifiedNotificationFromJson(message.value.toString());
            await this.update(event);
          } catch (err) {
            logger.error({ err }, "Failed to handle TableReservationModifiedNotification message");
          }
        }
      },
    });
  }

  override async update(event: TableReservationModifiedNotification): Promise<void> {
    await this.controller.handleTableReservationModified(event);
  }
}
