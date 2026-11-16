import { afterEach, describe, expect, it, vi } from "vitest";
import { Kafka } from "kafkajs";
import {
  TableReservationAcceptedNotification,
  TableReservationCanceledNotification,
  TableReservationModifiedNotification,
} from "@main/domain/external-modules";
import { KafkaTableReservationAcceptedNotificationConsumer } from "@main/infrastructure/adapter/inbound/kafka/KafkaTableReservationAcceptedNotificationConsumer";
import { KafkaTableReservationCanceledNotificationConsumer } from "@main/infrastructure/adapter/inbound/kafka/KafkaTableReservationCanceledNotificationConsumer";
import { KafkaTableReservationModifiedNotificationConsumer } from "@main/infrastructure/adapter/inbound/kafka/KafkaTableReservationModifiedNotificationConsumer";
import { NotificationController } from "@main/infrastructure/adapter/inbound/web/controller/controller";
import { logger } from "@main/infrastructure/adapter/outbound/logging/logger";

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

const payload = [
  "reservation-1",
  "restaurant-1",
  "customer-1",
  "2026-11-20T19:00:00.000Z",
  "2026-11-20T21:00:00.000Z",
  2,
  1,
] as const;

const cases = [
  {
    name: "accepted",
    Consumer: KafkaTableReservationAcceptedNotificationConsumer,
    event: new TableReservationAcceptedNotification(...payload),
    handler: "handleTableReservationAccepted",
  },
  {
    name: "modified",
    Consumer: KafkaTableReservationModifiedNotificationConsumer,
    event: new TableReservationModifiedNotification(...payload),
    handler: "handleTableReservationModified",
  },
  {
    name: "canceled",
    Consumer: KafkaTableReservationCanceledNotificationConsumer,
    event: new TableReservationCanceledNotification(...payload),
    handler: "handleTableReservationCanceled",
  },
] as const;

describe.each(cases)("table-reservation-$name consumer", ({ Consumer, event, handler }) => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  function build() {
    const { fakeKafka, fakeConsumer } = buildFakeKafka();
    const controller = new NotificationController();
    const handleSpy = vi.spyOn(controller, handler).mockResolvedValue(undefined);
    const consumer = new Consumer(fakeKafka, "topic", "group", controller);
    return { consumer, fakeConsumer, handleSpy };
  }

  it("subscribes to its topic from the beginning", async () => {
    const { consumer, fakeConsumer } = build();
    await consumer.connect();
    expect(fakeConsumer.connect).toHaveBeenCalled();
    expect(fakeConsumer.subscribe).toHaveBeenCalledWith({ topic: "topic", fromBeginning: true });
  });

  it("disconnects its consumer", async () => {
    const { consumer, fakeConsumer } = build();
    await consumer.disconnect();
    expect(fakeConsumer.disconnect).toHaveBeenCalled();
  });

  it("parses a message and forwards the event to the controller", async () => {
    const { consumer, fakeConsumer, handleSpy } = build();
    await consumer.run();

    const { eachMessage } = fakeConsumer.run.mock.calls[0][0];
    await eachMessage({ message: { value: Buffer.from(event.toJson()) } });

    expect(handleSpy).toHaveBeenCalledTimes(1);
    expect(handleSpy.mock.calls[0][0]).toMatchObject({
      reservationId: "reservation-1",
      tableNumber: 1,
    });
  });

  it("skips a message without a value", async () => {
    const { consumer, fakeConsumer, handleSpy } = build();
    await consumer.run();

    await fakeConsumer.run.mock.calls[0][0].eachMessage({ message: { value: null } });

    expect(handleSpy).not.toHaveBeenCalled();
  });

  it("logs a message it cannot parse and carries on", async () => {
    const { consumer, fakeConsumer, handleSpy } = build();
    const errorSpy = vi.spyOn(logger, "error").mockImplementation(() => {});
    await consumer.run();

    await fakeConsumer.run.mock.calls[0][0].eachMessage({
      message: { value: Buffer.from("invalid-json") },
    });

    expect(handleSpy).not.toHaveBeenCalled();
    expect(errorSpy).toHaveBeenCalledTimes(1);
  });
});

describe("NotificationController table reservation handlers", () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it.each(cases)("logs a $name reservation instead of sending a notification", async ({ event, handler }) => {
    const logSpy = vi.spyOn(logger, "info").mockImplementation(() => {});

    await new NotificationController()[handler](event as never);

    expect(logSpy).toHaveBeenCalledTimes(1);
    expect(JSON.stringify(logSpy.mock.calls[0])).toContain("reservation-1");
  });
});
