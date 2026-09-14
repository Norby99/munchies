import { beforeEach, describe, expect, it, vi } from "vitest";
import { KafkaPaymentNotificationPublisher } from "@main/infrastructure/adapter/outbound/kafka/KafkaPaymentNotificationPublisher";
import { Payment } from "@main/domain/model/Payment";
import { UUIDEntityId } from "munchies-commons/kotlin/commons-modules";
import {
  Currency,
  PaymentMethod,
  PaymentSuccessNotificationInfo,
} from "munchies-payment-service-shared/kotlin/payment-modules";

const connect = vi.fn().mockResolvedValue(undefined);
const send = vi.fn().mockResolvedValue(undefined);
const producer = vi.fn(() => ({ connect, send }));
const getKafka = vi.fn().mockResolvedValue({ producer });

vi.mock("@main/infrastructure/adapter/outbound/kafka/KafkaClient", () => ({
  default: (...args: unknown[]) => getKafka(...args),
}));

describe("KafkaPaymentNotificationPublisher", () => {
  beforeEach(() => {
    getKafka.mockClear();
    producer.mockClear();
    connect.mockClear();
    send.mockClear();
  });

  it("publishes a payment-success event on the shared topic", async () => {
    const publisher = new KafkaPaymentNotificationPublisher();
    const payment = Payment.create(
      new UUIDEntityId(),
      100,
      Currency.EUR,
      PaymentMethod.CARD
    ).complete();

    await publisher.publishPaymentSuccess(payment);

    expect(getKafka).toHaveBeenCalledWith(
      PaymentSuccessNotificationInfo.PAYMENT_SUCCESS_TOPIC
    );
    expect(connect).toHaveBeenCalledTimes(1);
    expect(send).toHaveBeenCalledTimes(1);

    const sentMessage = send.mock.calls[0][0];
    expect(sentMessage.topic).toBe(
      PaymentSuccessNotificationInfo.PAYMENT_SUCCESS_TOPIC
    );
    const payload = JSON.parse(sentMessage.messages[0].value);
    expect(payload.payment_id_key).toBe(payment.id.value);
    expect(payload.order_id_key).toBe(payment.orderId.value);
    expect(payload.amount_key).toBe(100);
  });

  it("connects the producer only once across multiple publications", async () => {
    const publisher = new KafkaPaymentNotificationPublisher();
    const payment = Payment.create(
      new UUIDEntityId(),
      50,
      Currency.USD,
      PaymentMethod.CARD
    ).complete();

    await publisher.publishPaymentSuccess(payment);
    await publisher.publishPaymentSuccess(payment);

    expect(getKafka).toHaveBeenCalledTimes(1);
    expect(connect).toHaveBeenCalledTimes(1);
    expect(send).toHaveBeenCalledTimes(2);
  });
});
