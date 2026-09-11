import { describe, expect, it, vi } from "vitest";
import { Kafka } from "kafkajs";
import { KafkaUserEmailConfirmationNotificationConsumer } from "@main/infrastructure/adapter/inbound/kafka/KafkaUserNotificationConsumer";
import { NotificationController } from "@main/infrastructure/adapter/inbound/web/controller/controller";
import { UserEmailConfirmationNotification } from "@main/domain/external-modules";

describe("KafkaUserEmailConfirmationNotificationConsumer", () => {
  it("forwards received events to the notification controller", () => {
    const fakeKafka = { consumer: () => ({}) } as unknown as Kafka;
    const controller = new NotificationController();
    const handleSpy = vi
      .spyOn(controller, "handleUserEmailConfirmation")
      .mockImplementation(() => {});

    const consumer = new KafkaUserEmailConfirmationNotificationConsumer(
      fakeKafka,
      "user_email_confirmation_topic",
      "user_email_confirmation_group_id",
      controller
    );

    const event = new UserEmailConfirmationNotification("user-1", "otk-123");
    consumer.update(event);

    expect(handleSpy).toHaveBeenCalledWith(event);
  });
});
