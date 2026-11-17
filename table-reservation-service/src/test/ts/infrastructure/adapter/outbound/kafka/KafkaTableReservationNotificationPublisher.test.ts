import { beforeEach, describe, expect, it, vi } from "vitest";
import {
  TableReservationNotificationInfo,
  tableReservationAcceptedNotificationFromJson,
} from "munchies-table-reservation-service-shared/kotlin/table-reservation-modules";
import { KafkaTableReservationNotificationPublisher } from "@main/infrastructure/adapter/outbound/kafka/KafkaTableReservationNotificationPublisher";
import { TableReservation } from "@main/domain/model/TableReservation";
import { TimeSlot } from "@main/domain/model/TimeSlot";
import { CUSTOMER, RESTAURANT, at } from "@test/support/fakes";
import { acceptedReservation } from "@test/support/reservations";

const connect = vi.fn();
const send = vi.fn();
const producer = vi.fn(() => ({ connect, send }));
const getKafka = vi.fn();

vi.mock("@main/infrastructure/adapter/outbound/kafka/KafkaClient", () => ({
  default: (...args: unknown[]) => getKafka(...args),
}));

describe("KafkaTableReservationNotificationPublisher", () => {
  const reservation = acceptedReservation({ time: "19:00", tableNumber: 3, guests: 4 });

  beforeEach(() => {
    vi.clearAllMocks();
    connect.mockResolvedValue(undefined);
    send.mockResolvedValue(undefined);
    getKafka.mockReturnValue({ producer });
  });

  it("publishes the whole accepted reservation on its topic", async () => {
    await new KafkaTableReservationNotificationPublisher().publishAccepted(reservation);

    expect(send).toHaveBeenCalledTimes(1);
    const { topic, messages } = send.mock.calls[0][0];
    expect(topic).toBe(TableReservationNotificationInfo.TABLE_RESERVATION_ACCEPTED_TOPIC);
    const event = tableReservationAcceptedNotificationFromJson(messages[0].value);
    expect(event.reservationId).toBe(reservation.id.value);
    expect(event.restaurantId).toBe(RESTAURANT);
    expect(event.customerId).toBe(CUSTOMER);
    expect(event.start).toBe("2026-11-20T19:00:00.000Z");
    expect(event.end).toBe("2026-11-20T21:00:00.000Z");
    expect(event.numberOfGuests).toBe(4);
    expect(event.tableNumber).toBe(3);
  });

  it("publishes modified and canceled reservations on their own topics", async () => {
    const publisher = new KafkaTableReservationNotificationPublisher();

    await publisher.publishModified(reservation);
    await publisher.publishCanceled(reservation.cancel());

    expect(send.mock.calls.map(([record]) => record.topic)).toEqual([
      TableReservationNotificationInfo.TABLE_RESERVATION_MODIFIED_TOPIC,
      TableReservationNotificationInfo.TABLE_RESERVATION_CANCELED_TOPIC,
    ]);
  });

  it("connects once and reuses the producer", async () => {
    const publisher = new KafkaTableReservationNotificationPublisher();

    await Promise.all([
      publisher.publishAccepted(reservation),
      publisher.publishModified(reservation),
    ]);

    expect(connect).toHaveBeenCalledTimes(1);
    expect(send).toHaveBeenCalledTimes(2);
  });

  it("connects again after a failed connection", async () => {
    const publisher = new KafkaTableReservationNotificationPublisher();
    connect.mockRejectedValueOnce(new Error("broker down"));

    await expect(publisher.publishAccepted(reservation)).rejects.toThrow("broker down");
    await publisher.publishAccepted(reservation);

    expect(connect).toHaveBeenCalledTimes(2);
    expect(send).toHaveBeenCalledTimes(1);
  });

  it("fails when Kafka is not configured", async () => {
    getKafka.mockImplementation(() => {
      throw new Error("Kafka is not online");
    });
    await expect(
      new KafkaTableReservationNotificationPublisher().publishAccepted(reservation),
    ).rejects.toThrow("Kafka is not online");
  });

  it("has nothing to notify for a reservation without a table", () => {
    const requested = TableReservation.request(
      RESTAURANT,
      CUSTOMER,
      TimeSlot.startingAt(at("19:00"), 120),
      2,
    );
    expect(() =>
      new KafkaTableReservationNotificationPublisher().publishAccepted(requested),
    ).toThrow("without a table");
  });
});
