import { describe, expect, it, vi } from "vitest";
import { Kafka } from "kafkajs";
import { KafkaPaymentSuccessNotificationConsumer } from "@main/infrastructure/adapter/inbound/kafka/KafkaPaymentSuccessNotificationConsumer";
import { NotificationController } from "@main/infrastructure/adapter/inbound/web/controller/controller";
import { PaymentSuccessNotification } from "@main/domain/external-modules";
import { Currency } from "munchies-payment-service-shared/kotlin/payment-modules";

describe("KafkaPaymentSuccessNotificationConsumer", () => {
  it("forwards received events to the notification controller", () => {
    const fakeKafka = { consumer: () => ({}) } as unknown as Kafka;
    const controller = new NotificationController();
    const handleSpy = vi
      .spyOn(controller, "handlePaymentSuccess")
      .mockImplementation(() => {});

    const consumer = new KafkaPaymentSuccessNotificationConsumer(
      fakeKafka,
      "payment_success_topic",
      "payment_success_group_id",
      controller
    );

    const event = new PaymentSuccessNotification(
      "payment-1",
      "order-1",
      100,
      Currency.EUR
    );
    consumer.update(event);

    expect(handleSpy).toHaveBeenCalledWith(event);
  });
});
