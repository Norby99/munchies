import { describe, expect, it, vi, afterEach } from "vitest";
import { Kafka } from "kafkajs";
import { KafkaPaymentSuccessNotificationConsumer } from "@main/infrastructure/adapter/inbound/kafka/KafkaPaymentSuccessNotificationConsumer";
import { NotificationController } from "@main/infrastructure/adapter/inbound/web/controller/controller";
import { PaymentSuccessNotification, paymentSuccessNotificationFromJson } from "@main/domain/external-modules";
import { Currency } from "munchies-payment-service-shared/kotlin/payment-modules";

/** Build a fake Kafka instance backed by a controllable fake consumer. */
function buildFakeKafka() {
  const fakeConsumer = {
    connect: vi.fn().mockResolvedValue(undefined),
    disconnect: vi.fn().mockResolvedValue(undefined),
    subscribe: vi.fn().mockResolvedValue(undefined),
    run: vi.fn().mockResolvedValue(undefined),
  };
  const fakeKafka = { consumer: vi.fn().mockReturnValue(fakeConsumer) } as unknown as Kafka;
  return { fakeKafka, fakeConsumer };
}

describe("KafkaPaymentSuccessNotificationConsumer", () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("forwards received events to the notification controller via update()", () => {
    const { fakeKafka } = buildFakeKafka();
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

  it("connect() calls consumer.connect() and subscribes to the topic", async () => {
    const { fakeKafka, fakeConsumer } = buildFakeKafka();
    const controller = new NotificationController();

    const consumer = new KafkaPaymentSuccessNotificationConsumer(
      fakeKafka,
      "payment_success_topic",
      "payment_success_group_id",
      controller
    );

    await consumer.connect();

    expect(fakeConsumer.connect).toHaveBeenCalled();
    expect(fakeConsumer.subscribe).toHaveBeenCalledWith({
      topic: "payment_success_topic",
      fromBeginning: true,
    });
  });

  it("disconnect() calls consumer.disconnect()", async () => {
    const { fakeKafka, fakeConsumer } = buildFakeKafka();
    const controller = new NotificationController();

    const consumer = new KafkaPaymentSuccessNotificationConsumer(
      fakeKafka,
      "payment_success_topic",
      "payment_success_group_id",
      controller
    );

    await consumer.disconnect();

    expect(fakeConsumer.disconnect).toHaveBeenCalled();
  });

  it("run() registers eachMessage handler that parses and forwards a valid message", async () => {
    const { fakeKafka, fakeConsumer } = buildFakeKafka();
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

    await consumer.run();

    // Simulate eachMessage being called with a message containing valid JSON
    const runCall = fakeConsumer.run.mock.calls[0][0];
    const event = new PaymentSuccessNotification("payment-1", "order-1", 100, Currency.EUR);
    const messageValue = Buffer.from(event.toJson());
    await runCall.eachMessage({ message: { value: messageValue } });

    expect(handleSpy).toHaveBeenCalled();
  });

  it("run() eachMessage handler skips messages with null value", async () => {
    const { fakeKafka, fakeConsumer } = buildFakeKafka();
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

    await consumer.run();

    const runCall = fakeConsumer.run.mock.calls[0][0];
    await runCall.eachMessage({ message: { value: null } });

    expect(handleSpy).not.toHaveBeenCalled();
  });

  it("run() eachMessage handler logs an error when message parsing fails", async () => {
    const { fakeKafka, fakeConsumer } = buildFakeKafka();
    const controller = new NotificationController();
    const consoleErrorSpy = vi.spyOn(console, "error").mockImplementation(() => {});

    const consumer = new KafkaPaymentSuccessNotificationConsumer(
      fakeKafka,
      "payment_success_topic",
      "payment_success_group_id",
      controller
    );

    await consumer.run();

    const runCall = fakeConsumer.run.mock.calls[0][0];
    // Pass an invalid JSON that will cause paymentSuccessNotificationFromJson to throw
    await runCall.eachMessage({ message: { value: Buffer.from("invalid-json") } });

    expect(consoleErrorSpy).toHaveBeenCalledWith(
      expect.stringContaining("Failed to parse PaymentSuccessNotification message"),
      expect.anything()
    );
  });
});
