import {
  UserEmailConfirmationNotificationSubject,
  _UserEmailConfirmationNotificationSubject,
  _UserEmailConfirmationNotificationObserver,
  _UserEmailConfirmationNotification,
  UserEmailConfirmationNotification,
  UserEmailConfirmationTopic,
} from "@main/domain/external-modules";
import { Kafka, Producer } from "kafkajs";

export class KafkaUserNotificationProducer extends _UserEmailConfirmationNotificationSubject {
  private observers = new Set<_UserEmailConfirmationNotificationObserver>();
  private producer: Producer;
  private topic: string = UserEmailConfirmationTopic;

  constructor(kafka: Kafka) {
    super();
    this.producer = kafka.producer();
  }

  async connect() {
    await this.producer.connect();
  }

  async disconnect() {
    await this.producer.disconnect();
  }

  override attach(observer: _UserEmailConfirmationNotificationObserver): void {
    this.observers.add(observer);
  }

  override detach(observer: _UserEmailConfirmationNotificationObserver): void {
    this.observers.delete(observer);
  }

  override async emit(
    event: _UserEmailConfirmationNotification
  ): Promise<void> {
    await this.producer.send({
      topic: this.topic,
      messages: [{ value: event.toJson() }],
    });
  }
}
