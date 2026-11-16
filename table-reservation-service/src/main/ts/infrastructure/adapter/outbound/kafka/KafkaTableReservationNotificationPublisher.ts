import { Producer } from "kafkajs";
import {
  TableReservationAcceptedNotification,
  TableReservationCanceledNotification,
  TableReservationModifiedNotification,
  TableReservationNotificationInfo,
} from "munchies-table-reservation-service-shared/kotlin/table-reservation-modules";
import { TableReservation } from "@main/domain/model/TableReservation";
import { TableReservationNotificationPublisher } from "@main/domain/port/table-reservation-notification-publisher";
import getKafka from "./KafkaClient";

const CLIENT_ID = "table-reservation-service";

/**
 * Kafka adapter publishing the reservation events notification-service consumes, one topic per
 * event (accepted, modified, canceled), as named in table-reservation-shared.
 *
 * The producer connection is established lazily, on first use, and reused afterwards. A failed
 * connection is not cached, so the next event tries again.
 */
export class KafkaTableReservationNotificationPublisher
  implements TableReservationNotificationPublisher
{
  private producer: Promise<Producer> | null = null;

  private getProducer(): Promise<Producer> {
    if (!this.producer) {
      const connecting = (async () => {
        const producer = getKafka(CLIENT_ID).producer();
        await producer.connect();
        return producer;
      })();
      connecting.catch(() => {
        if (this.producer === connecting) this.producer = null;
      });
      this.producer = connecting;
    }
    return this.producer;
  }

  private async send(topic: string, json: string): Promise<void> {
    const producer = await this.getProducer();
    await producer.send({ topic, messages: [{ value: json }] });
  }

  publishAccepted(reservation: TableReservation): Promise<void> {
    return this.send(
      TableReservationNotificationInfo.TABLE_RESERVATION_ACCEPTED_TOPIC,
      new TableReservationAcceptedNotification(...payloadOf(reservation)).toJson(),
    );
  }

  publishModified(reservation: TableReservation): Promise<void> {
    return this.send(
      TableReservationNotificationInfo.TABLE_RESERVATION_MODIFIED_TOPIC,
      new TableReservationModifiedNotification(...payloadOf(reservation)).toJson(),
    );
  }

  publishCanceled(reservation: TableReservation): Promise<void> {
    return this.send(
      TableReservationNotificationInfo.TABLE_RESERVATION_CANCELED_TOPIC,
      new TableReservationCanceledNotification(...payloadOf(reservation)).toJson(),
    );
  }
}

/**
 * The fields every reservation event carries, in the order of the shared notification
 * constructors: reservation, restaurant, customer, slot start and end, guests, table.
 */
function payloadOf(
  reservation: TableReservation,
): [string, string, string, string, string, number, number] {
  if (reservation.tableNumber === null) {
    throw new Error("A reservation without a table has nothing to notify");
  }
  return [
    reservation.id.value,
    reservation.restaurantId,
    reservation.customerId,
    reservation.slot.start.toISOString(),
    reservation.slot.end.toISOString(),
    reservation.numberOfGuests,
    reservation.tableNumber,
  ];
}
