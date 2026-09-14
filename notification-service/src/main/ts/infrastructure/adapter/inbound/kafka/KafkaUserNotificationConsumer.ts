import {
  _UserEmailConfirmationNotification,
  _UserEmailConfirmationNotificationObserver,
  getUserEmailConfirmationNotificationFromJson,
} from "@main/domain/external-modules";
import { NotificationController } from "@main/infrastructure/adapter/inbound/web/controller/controller";
import { Kafka, Consumer } from "kafkajs";

/**
 * Kafka consumer for the user email-confirmation topic. Parses incoming
 * messages and forwards them to the shared {@link NotificationController}.
 */
export class KafkaUserEmailConfirmationNotificationConsumer extends _UserEmailConfirmationNotificationObserver {
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
            const event = getUserEmailConfirmationNotificationFromJson(
              message.value.toString()
            );
            this.update(event);
          } catch (err) {
            console.error(
              "Failed to parse UserEmailConfirmationNotification message",
              err
            );
          }
        }
      },
    });
  }

  override update(event: _UserEmailConfirmationNotification): void {
    this.controller.handleUserEmailConfirmation(event);
  }
}
