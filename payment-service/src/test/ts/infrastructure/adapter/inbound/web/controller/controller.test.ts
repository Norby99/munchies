import { describe, expect, it, vi } from "vitest";
import { PaymentController } from "@main/infrastructure/adapter/inbound/web/controller/controller";
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
import { ProcessPayment } from "@main/application/port/inbound/ProcessPayment";

/**
 * The controller only translates HTTP to and from the use case: the follow-up steps of a
 * payment (order-service and notification-service) are covered by the use case tests.
 */
describe("PaymentController", () => {
  const useCaseWithStubbedPorts = (): ProcessPayment =>
    new ProcessPaymentUseCase(
      new InMemoryPaymentRepository(),
      new FakePaymentGateway(),
      { markOrderAsPaid: vi.fn().mockResolvedValue({ success: true }) },
      { publishPaymentSuccess: vi.fn().mockResolvedValue(undefined) },
      { info: vi.fn(), warn: vi.fn(), error: vi.fn() }
    );

  it("returns the payment response when the use case succeeds", async () => {
    const controller = new PaymentController(useCaseWithStubbedPorts());

    const request = new ProcessPaymentRequest(
      "order-test-123",
      new PaymentDetails(250, PaymentMethod.CARD, Currency.AUD)
    );

    const response = await controller.processPayment(request);

    expect(response.status).toBe(PaymentStatus.COMPLETED);
    expect(response.amount).toBe(250);
    expect(response.currency).toBe(Currency.AUD);
    expect(response.paymentId).toBeTruthy();
  });

  it("throws an error when the use case returns an invalid request", async () => {
    const mockUseCase: ProcessPayment = {
      execute: async () => ({
        type: "INVALID_REQUEST",
        reason: "Invalid order ID",
      }),
    };

    const controller = new PaymentController(mockUseCase);
    const request = new ProcessPaymentRequest(
      "",
      new PaymentDetails(100, PaymentMethod.CARD, Currency.AUD)
    );

    await expect(controller.processPayment(request)).rejects.toThrow(
      "Invalid order ID"
    );
  });

  it("throws an error when the payment is rejected", async () => {
    const mockUseCase: ProcessPayment = {
      execute: async () => ({
        type: "PAYMENT_REJECTED",
        reason: "Card declined",
      }),
    };

    const controller = new PaymentController(mockUseCase);
    const request = new ProcessPaymentRequest(
      "order-test-999",
      new PaymentDetails(100, PaymentMethod.CARD, Currency.AUD)
    );

    await expect(controller.processPayment(request)).rejects.toThrow(
      "Card declined"
    );
  });
});
