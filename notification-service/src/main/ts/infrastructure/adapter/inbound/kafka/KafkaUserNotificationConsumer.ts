import {
  _UserEmailConfirmationNotification,
  _UserEmailConfirmationNotificationObserver,
  UserEmailConfirmationNotification,
  UserEmailConfirmationNotificationSubject,
  getUserEmailConfirmationNotificationFromJson,
} from "@main/domain/external-modules";
import { Kafka, Consumer } from "kafkajs";

export class KafkaUserEmailConfirmationNotificationConsumer extends _UserEmailConfirmationNotificationObserver {
  private consumer: Consumer;
  private observers = new Set<_UserEmailConfirmationNotificationObserver>();
  private topic: string;

  constructor(kafka: Kafka, topic: string, groupId: string) {
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
            console.log("Failed to parse" + message);
          }
        }
      },
    });
  }

  override update(event: _UserEmailConfirmationNotification): void {
    console.log("Ha funzionato" + event.toString());
  }
}
