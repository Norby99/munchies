import { describe, expect, it, vi, afterEach } from "vitest";
import { Kafka } from "kafkajs";
import { KafkaUserEmailConfirmationNotificationConsumer } from "@main/infrastructure/adapter/inbound/kafka/KafkaUserNotificationConsumer";
import { NotificationController } from "@main/infrastructure/adapter/inbound/web/controller/controller";
import { UserEmailConfirmationNotification } from "@main/domain/external-modules";

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

describe("KafkaUserEmailConfirmationNotificationConsumer", () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("forwards received events to the notification controller via update()", () => {
    const { fakeKafka } = buildFakeKafka();
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

  it("connect() calls consumer.connect() and subscribes to the topic", async () => {
    const { fakeKafka, fakeConsumer } = buildFakeKafka();
    const controller = new NotificationController();

    const consumer = new KafkaUserEmailConfirmationNotificationConsumer(
      fakeKafka,
      "user_email_confirmation_topic",
      "user_email_confirmation_group_id",
      controller
    );

    await consumer.connect();

    expect(fakeConsumer.connect).toHaveBeenCalled();
    expect(fakeConsumer.subscribe).toHaveBeenCalledWith({
      topic: "user_email_confirmation_topic",
      fromBeginning: true,
    });
  });

  it("disconnect() calls consumer.disconnect()", async () => {
    const { fakeKafka, fakeConsumer } = buildFakeKafka();
    const controller = new NotificationController();

    const consumer = new KafkaUserEmailConfirmationNotificationConsumer(
      fakeKafka,
      "user_email_confirmation_topic",
      "user_email_confirmation_group_id",
      controller
    );

    await consumer.disconnect();

    expect(fakeConsumer.disconnect).toHaveBeenCalled();
  });

  it("run() registers eachMessage handler that parses and forwards a valid message", async () => {
    const { fakeKafka, fakeConsumer } = buildFakeKafka();
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

    await consumer.run();

    const runCall = fakeConsumer.run.mock.calls[0][0];
    const event = new UserEmailConfirmationNotification("user-1", "otk-123");
    const messageValue = Buffer.from(event.toJson());
    await runCall.eachMessage({ message: { value: messageValue } });

    expect(handleSpy).toHaveBeenCalled();
  });

  it("run() eachMessage handler skips messages with null value", async () => {
    const { fakeKafka, fakeConsumer } = buildFakeKafka();
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

    await consumer.run();

    const runCall = fakeConsumer.run.mock.calls[0][0];
    await runCall.eachMessage({ message: { value: null } });

    expect(handleSpy).not.toHaveBeenCalled();
  });

  it("run() eachMessage handler logs an error when message parsing fails", async () => {
    const { fakeKafka, fakeConsumer } = buildFakeKafka();
    const controller = new NotificationController();
    const consoleErrorSpy = vi.spyOn(console, "error").mockImplementation(() => {});

    const consumer = new KafkaUserEmailConfirmationNotificationConsumer(
      fakeKafka,
      "user_email_confirmation_topic",
      "user_email_confirmation_group_id",
      controller
    );

    await consumer.run();

    const runCall = fakeConsumer.run.mock.calls[0][0];
    // Pass an invalid JSON that will cause getUserEmailConfirmationNotificationFromJson to throw
    await runCall.eachMessage({ message: { value: Buffer.from("invalid-json") } });

    expect(consoleErrorSpy).toHaveBeenCalledWith(
      expect.stringContaining("Failed to parse UserEmailConfirmationNotification message"),
      expect.anything()
    );
  });
});

