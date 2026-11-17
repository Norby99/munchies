import { describe, expect, it, beforeEach, vi } from "vitest";
import { ProcessPaymentUseCase } from "@main/application/usecase/ProcessPaymentUseCase";
import { InMemoryPaymentRepository } from "@main/infrastructure/adapter/outbound/memory/InMemoryPaymentRepository";
import { FakePaymentGateway } from "@main/infrastructure/adapter/outbound/payment/FakePaymentGateway";
import {
  Currency,
  PaymentDetails,
  PaymentMethod,
  PaymentStatus,
  ProcessPaymentRequest,
} from "munchies-payment-service-shared/kotlin/payment-modules";
import { UUIDEntityId } from "munchies-commons/kotlin/commons-modules";
import { OrderServiceClient } from "@main/domain/port/order-service-client";
import { PaymentNotificationPublisher } from "@main/domain/port/payment-notification-publisher";
import { Logger } from "@main/domain/port/logger";

const CUSTOMER = "customer-1";

/** order-service's view of the order being paid: the caller's, unpaid, 1.50 EUR. */
const unpaidOrder = { customerId: CUSTOMER, payed: false, totalCents: 150 };

describe("ProcessPaymentUseCase", () => {
  let repository: InMemoryPaymentRepository;
  let gateway: FakePaymentGateway;
  let orderServiceClient: OrderServiceClient;
  let publisher: PaymentNotificationPublisher;
  let logger: Logger;
  let useCase: ProcessPaymentUseCase;

  beforeEach(() => {
    repository = new InMemoryPaymentRepository();
    gateway = new FakePaymentGateway();
    orderServiceClient = {
      getOrder: vi.fn().mockResolvedValue({ type: "FOUND", order: unpaidOrder }),
      markOrderAsPaid: vi.fn().mockResolvedValue({ success: true }),
    };
    publisher = {
      publishPaymentSuccess: vi.fn().mockResolvedValue(undefined),
    };
    logger = { info: vi.fn(), warn: vi.fn(), error: vi.fn() };
    useCase = new ProcessPaymentUseCase(
      repository,
      gateway,
      orderServiceClient,
      publisher,
      logger
    );
  });

  it("successfully processes a card payment", async () => {
    const request = new ProcessPaymentRequest(
      "order-123",
      CUSTOMER,
      new PaymentDetails(PaymentMethod.CARD)
    );

    const result = await useCase.execute(request);

    expect(result.type).toBe("SUCCESS");
    if (result.type === "SUCCESS") {
      expect(result.payment.status).toBe(PaymentStatus.COMPLETED);
      expect(result.payment.amount).toBe(150);
      // The amount and currency come from the order, not from the client.
      expect(orderServiceClient.getOrder).toHaveBeenCalledExactlyOnceWith("order-123");
      expect(result.payment.currency).toBe(Currency.EUR);
      expect(result.payment.method).toBe(PaymentMethod.CARD);
      expect(result.payment.payedAt).not.toBeNull();

      expect(result.response.amount).toBe(150);
      expect(result.response.status).toBe(PaymentStatus.COMPLETED);

      const saved = await repository.findById(result.payment.id);
      expect(saved).toEqual(result.payment);

      expect(logger.info).toHaveBeenCalledExactlyOnceWith("Payment completed", {
        paymentId: result.payment.id.value,
        orderId: "order-123",
        amount: 150,
        currency: "EUR",
        method: "CARD",
      });
    }
  });

  it("successfully processes cash, mobile, bank transfer, and other payment methods", async () => {
    const methods = [
      PaymentMethod.CASH,
      PaymentMethod.MOBILE_PAYMENT,
      PaymentMethod.BANK_TRANSFER,
      PaymentMethod.OTHER,
    ];

    for (const method of methods) {
      const request = new ProcessPaymentRequest(
        `order-${method.name}`,
        CUSTOMER,
        new PaymentDetails(method)
      );

      const result = await useCase.execute(request);
      expect(result.type).toBe("SUCCESS");
      if (result.type === "SUCCESS") {
        expect(result.payment.method).toBe(method);
        expect(result.payment.status).toBe(PaymentStatus.COMPLETED);
      }
    }
  });

  it("returns INVALID_REQUEST when orderId is missing", async () => {
    const request = new ProcessPaymentRequest(
      "",
      CUSTOMER,
      new PaymentDetails(PaymentMethod.CARD)
    );

    const result = await useCase.execute(request);

    expect(result.type).toBe("INVALID_REQUEST");
  });

  it("returns INVALID_REQUEST when the customer id is missing", async () => {
    const request = new ProcessPaymentRequest(
      "order-123",
      "",
      new PaymentDetails(PaymentMethod.CARD)
    );

    const result = await useCase.execute(request);

    expect(result.type).toBe("INVALID_REQUEST");
  });

  it("returns PAYMENT_REJECTED and stores failed payment when gateway fails", async () => {
    const failingGateway = new FakePaymentGateway({
      simulateFailure: true,
      failureReason: "Card declined by issuer",
    });
    const failingUseCase = new ProcessPaymentUseCase(
      repository,
      failingGateway,
      orderServiceClient,
      publisher,
      logger
    );

    const request = new ProcessPaymentRequest(
      "order-456",
      CUSTOMER,
      new PaymentDetails(PaymentMethod.CARD)
    );

    const result = await failingUseCase.execute(request);

    expect(result.type).toBe("PAYMENT_REJECTED");
    if (result.type === "PAYMENT_REJECTED") {
      expect(result.reason).toBe("Card declined by issuer");
    }

    const saved = await repository.findByOrderId(new UUIDEntityId("order-456"));
    expect(saved).not.toBeNull();
    expect(saved?.status).toBe(PaymentStatus.FAILED);
    expect(orderServiceClient.markOrderAsPaid).not.toHaveBeenCalled();
    expect(publisher.publishPaymentSuccess).not.toHaveBeenCalled();
  });

  // ---------- Follow-up steps of a successful payment ----------

  it("marks the order as paid and publishes the payment-success event after saving", async () => {
    const request = new ProcessPaymentRequest(
      "order-notify",
      CUSTOMER,
      new PaymentDetails(PaymentMethod.CARD)
    );

    const result = await useCase.execute(request);

    expect(result.type).toBe("SUCCESS");
    if (result.type === "SUCCESS") {
      expect(orderServiceClient.markOrderAsPaid).toHaveBeenCalledExactlyOnceWith(
        "order-notify"
      );
      expect(publisher.publishPaymentSuccess).toHaveBeenCalledExactlyOnceWith(
        result.payment
      );
      // The event is published only once the completed payment is durable.
      const saved = await repository.findById(result.payment.id);
      expect(saved?.status).toBe(PaymentStatus.COMPLETED);
    }
  });

  it("does not run the follow-up steps when the request is invalid", async () => {
    const request = new ProcessPaymentRequest(
      "",
      CUSTOMER,
      new PaymentDetails(PaymentMethod.CARD)
    );

    await useCase.execute(request);

    expect(orderServiceClient.markOrderAsPaid).not.toHaveBeenCalled();
    expect(publisher.publishPaymentSuccess).not.toHaveBeenCalled();
  });

  it("still succeeds and publishes the event when order-service cannot be notified", async () => {
    orderServiceClient.markOrderAsPaid = vi.fn().mockResolvedValue({
      success: false,
      errorMessage: "order-service unreachable",
    });

    const result = await useCase.execute(
      new ProcessPaymentRequest(
        "order-unreachable",
        CUSTOMER,
        new PaymentDetails(PaymentMethod.CARD)
      )
    );

    expect(result.type).toBe("SUCCESS");
    expect(publisher.publishPaymentSuccess).toHaveBeenCalledTimes(1);
    expect(logger.error).toHaveBeenCalledExactlyOnceWith(
      "Failed to notify order-service that the order was paid",
      { orderId: "order-unreachable", reason: "order-service unreachable" }
    );
  });

  it("still succeeds when order-service client throws", async () => {
    orderServiceClient.markOrderAsPaid = vi
      .fn()
      .mockRejectedValue(new Error("connection refused"));

    const result = await useCase.execute(
      new ProcessPaymentRequest(
        "order-throws",
        CUSTOMER,
        new PaymentDetails(PaymentMethod.CARD)
      )
    );

    expect(result.type).toBe("SUCCESS");
    expect(publisher.publishPaymentSuccess).toHaveBeenCalledTimes(1);
  });

  it("still succeeds when publishing the payment-success event fails", async () => {
    publisher.publishPaymentSuccess = vi
      .fn()
      .mockRejectedValue(new Error("Kafka is not online"));

    const result = await useCase.execute(
      new ProcessPaymentRequest(
        "order-kafka-down",
        CUSTOMER,
        new PaymentDetails(PaymentMethod.CARD)
      )
    );

    expect(result.type).toBe("SUCCESS");
    if (result.type === "SUCCESS") {
      expect(result.payment.status).toBe(PaymentStatus.COMPLETED);
    }
  });

  // ---------- Checks on the order, before any money moves ----------

  const payOrder = () =>
    useCase.execute(
      new ProcessPaymentRequest("order-1", CUSTOMER, new PaymentDetails(PaymentMethod.CARD))
    );

  const expectNothingCharged = async () => {
    expect(await repository.findByOrderId(new UUIDEntityId("order-1"))).toBeNull();
    expect(orderServiceClient.markOrderAsPaid).not.toHaveBeenCalled();
    expect(publisher.publishPaymentSuccess).not.toHaveBeenCalled();
  };

  it("charges the order total read from order-service", async () => {
    orderServiceClient.getOrder = vi.fn().mockResolvedValue({
      type: "FOUND",
      order: { ...unpaidOrder, totalCents: 3100 },
    });

    const result = await payOrder();

    expect(result.type).toBe("SUCCESS");
    if (result.type === "SUCCESS") expect(result.payment.amount).toBe(3100);
  });

  it("returns ORDER_NOT_FOUND for an unknown order", async () => {
    orderServiceClient.getOrder = vi.fn().mockResolvedValue({ type: "NOT_FOUND" });

    expect((await payOrder()).type).toBe("ORDER_NOT_FOUND");
    await expectNothingCharged();
  });

  it("returns FORBIDDEN for another customer's order", async () => {
    orderServiceClient.getOrder = vi.fn().mockResolvedValue({
      type: "FOUND",
      order: { ...unpaidOrder, customerId: "someone-else" },
    });

    expect((await payOrder()).type).toBe("FORBIDDEN");
    await expectNothingCharged();
  });

  it("returns ALREADY_PAID for a paid order, so a double submit cannot charge twice", async () => {
    orderServiceClient.getOrder = vi.fn().mockResolvedValue({
      type: "FOUND",
      order: { ...unpaidOrder, payed: true },
    });

    expect((await payOrder()).type).toBe("ALREADY_PAID");
    await expectNothingCharged();
  });

  it("returns NOTHING_TO_PAY for an order with a zero total", async () => {
    orderServiceClient.getOrder = vi.fn().mockResolvedValue({
      type: "FOUND",
      order: { ...unpaidOrder, totalCents: 0 },
    });

    expect((await payOrder()).type).toBe("NOTHING_TO_PAY");
    await expectNothingCharged();
  });

  it("returns ORDER_UNAVAILABLE when order-service cannot be asked", async () => {
    orderServiceClient.getOrder = vi
      .fn()
      .mockResolvedValue({ type: "UNAVAILABLE", reason: "connection refused" });

    expect((await payOrder()).type).toBe("ORDER_UNAVAILABLE");
    await expectNothingCharged();
  });
});
