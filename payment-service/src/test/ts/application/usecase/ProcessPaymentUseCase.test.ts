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

describe("ProcessPaymentUseCase", () => {
  let repository: InMemoryPaymentRepository;
  let gateway: FakePaymentGateway;
  let orderServiceClient: OrderServiceClient;
  let publisher: PaymentNotificationPublisher;
  let useCase: ProcessPaymentUseCase;

  beforeEach(() => {
    repository = new InMemoryPaymentRepository();
    gateway = new FakePaymentGateway();
    orderServiceClient = {
      markOrderAsPaid: vi.fn().mockResolvedValue({ success: true }),
    };
    publisher = {
      publishPaymentSuccess: vi.fn().mockResolvedValue(undefined),
    };
    useCase = new ProcessPaymentUseCase(
      repository,
      gateway,
      orderServiceClient,
      publisher
    );
  });

  it("successfully processes a card payment", async () => {
    const request = new ProcessPaymentRequest(
      "order-123",
      new PaymentDetails(150, PaymentMethod.CARD, Currency.EUR)
    );

    const result = await useCase.execute(request);

    expect(result.type).toBe("SUCCESS");
    if (result.type === "SUCCESS") {
      expect(result.payment.status).toBe(PaymentStatus.COMPLETED);
      expect(result.payment.amount).toBe(150);
      expect(result.payment.currency).toBe(Currency.EUR);
      expect(result.payment.method).toBe(PaymentMethod.CARD);
      expect(result.payment.payedAt).not.toBeNull();

      expect(result.response.amount).toBe(150);
      expect(result.response.status).toBe(PaymentStatus.COMPLETED);

      const saved = await repository.findById(result.payment.id);
      expect(saved).toEqual(result.payment);
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
        new PaymentDetails(200, method, Currency.USD)
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
      new PaymentDetails(100, PaymentMethod.CARD, Currency.USD)
    );

    const result = await useCase.execute(request);

    expect(result.type).toBe("INVALID_REQUEST");
  });

  it("returns INVALID_REQUEST when amount is negative", async () => {
    const request = new ProcessPaymentRequest(
      "order-123",
      new PaymentDetails(-50, PaymentMethod.CARD, Currency.USD)
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
      publisher
    );

    const request = new ProcessPaymentRequest(
      "order-456",
      new PaymentDetails(100, PaymentMethod.CARD, Currency.USD)
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
      new PaymentDetails(50, PaymentMethod.CARD, Currency.EUR)
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
      new PaymentDetails(100, PaymentMethod.CARD, Currency.USD)
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
        new PaymentDetails(75, PaymentMethod.CARD, Currency.AUD)
      )
    );

    expect(result.type).toBe("SUCCESS");
    expect(publisher.publishPaymentSuccess).toHaveBeenCalledTimes(1);
  });

  it("still succeeds when order-service client throws", async () => {
    orderServiceClient.markOrderAsPaid = vi
      .fn()
      .mockRejectedValue(new Error("connection refused"));

    const result = await useCase.execute(
      new ProcessPaymentRequest(
        "order-throws",
        new PaymentDetails(75, PaymentMethod.CARD, Currency.AUD)
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
        new PaymentDetails(50, PaymentMethod.CARD, Currency.EUR)
      )
    );

    expect(result.type).toBe("SUCCESS");
    if (result.type === "SUCCESS") {
      expect(result.payment.status).toBe(PaymentStatus.COMPLETED);
    }
  });
});
