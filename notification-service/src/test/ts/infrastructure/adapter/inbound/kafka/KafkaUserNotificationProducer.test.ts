import { describe, expect, it, vi, afterEach } from "vitest";
import { Kafka } from "kafkajs";
import { KafkaUserNotificationProducer } from "@main/infrastructure/adapter/inbound/kafka/KafkaUserNotificationProducer";
import {
  UserEmailConfirmationNotification,
  _UserEmailConfirmationNotificationObserver,
} from "@main/domain/external-modules";

/** Build a fake Kafka instance backed by a controllable fake producer. */
function buildFakeKafka() {
  const fakeProducer = {
    connect: vi.fn().mockResolvedValue(undefined),
    disconnect: vi.fn().mockResolvedValue(undefined),
    send: vi.fn().mockResolvedValue(undefined),
  };
  const fakeKafka = { producer: vi.fn().mockReturnValue(fakeProducer) } as unknown as Kafka;
  return { fakeKafka, fakeProducer };
}

describe("KafkaUserNotificationProducer", () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("connect() calls producer.connect()", async () => {
    const { fakeKafka, fakeProducer } = buildFakeKafka();
    const producer = new KafkaUserNotificationProducer(fakeKafka);

    await producer.connect();

    expect(fakeProducer.connect).toHaveBeenCalled();
  });

  it("disconnect() calls producer.disconnect()", async () => {
    const { fakeKafka, fakeProducer } = buildFakeKafka();
    const producer = new KafkaUserNotificationProducer(fakeKafka);

    await producer.disconnect();

    expect(fakeProducer.disconnect).toHaveBeenCalled();
  });

  it("attach() adds an observer to the internal set", () => {
    const { fakeKafka } = buildFakeKafka();
    const producer = new KafkaUserNotificationProducer(fakeKafka);

    // Cast to access internal state for verification
    const observer = {} as _UserEmailConfirmationNotificationObserver;
    producer.attach(observer);

    // We verify indirectly: detach should succeed without error (meaning the observer was added)
    expect(() => producer.detach(observer)).not.toThrow();
  });

  it("detach() removes an observer from the internal set", () => {
    const { fakeKafka } = buildFakeKafka();
    const producer = new KafkaUserNotificationProducer(fakeKafka);
    const observer = {} as _UserEmailConfirmationNotificationObserver;

    producer.attach(observer);
    producer.detach(observer);

    // Attaching and detaching twice should not throw
    expect(() => producer.attach(observer)).not.toThrow();
  });

  it("emit() sends the event serialized as JSON to the correct topic", async () => {
    const { fakeKafka, fakeProducer } = buildFakeKafka();
    const producer = new KafkaUserNotificationProducer(fakeKafka);
    const event = new UserEmailConfirmationNotification("user-1", "otk-123");

    await producer.emit(event);

    expect(fakeProducer.send).toHaveBeenCalledWith(
      expect.objectContaining({
        messages: [{ value: event.toJson() }],
      })
    );
  });
});
