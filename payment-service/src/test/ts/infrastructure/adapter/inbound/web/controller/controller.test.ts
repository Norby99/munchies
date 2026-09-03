import { describe, expect, it } from "vitest";
import { PaymentController } from "@main/infrastructure/adapter/inbound/web/controller/controller";
import { PaymentBeans } from "@main/infrastructure/adapter/inbound/web/config/PaymentBeans";
import {
  Currency,
  PaymentDetails,
  PaymentMethod,
  PaymentStatus,
  ProcessPaymentRequest,
} from "munchies-payment-service-shared/kotlin/payment-modules";
import { ProcessPayment } from "@main/application/port/inbound/ProcessPayment";

describe("PaymentController", () => {
  it("processes a payment successfully using in-memory beans", async () => {
    const beans = PaymentBeans.createInMemoryBeans();
    const controller = new PaymentController(beans.paymentServices.processPayment);

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

  it("throws an error when use case returns failure", async () => {
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
});
