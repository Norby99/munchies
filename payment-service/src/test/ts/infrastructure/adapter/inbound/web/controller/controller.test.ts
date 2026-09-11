import { describe, expect, it, vi } from "vitest";
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
import { OrderServiceClient } from "@main/domain/port/order-service-client";

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

  it("notifies order-service that the order was paid once payment succeeds", async () => {
    const beans = PaymentBeans.createInMemoryBeans();
    const orderServiceClient: OrderServiceClient = {
      markOrderAsPaid: vi.fn().mockResolvedValue({ success: true }),
    };
    const controller = new PaymentController(
      beans.paymentServices.processPayment,
      orderServiceClient
    );

    const request = new ProcessPaymentRequest(
      "order-test-456",
      new PaymentDetails(75, PaymentMethod.CARD, Currency.AUD)
    );

    await controller.processPayment(request);

    expect(orderServiceClient.markOrderAsPaid).toHaveBeenCalledTimes(1);
    expect(orderServiceClient.markOrderAsPaid).toHaveBeenCalledWith(
      "order-test-456"
    );
  });

  it("still returns the payment response when order-service notification fails", async () => {
    const beans = PaymentBeans.createInMemoryBeans();
    const orderServiceClient: OrderServiceClient = {
      markOrderAsPaid: vi.fn().mockResolvedValue({
        success: false,
        errorMessage: "order-service unreachable",
      }),
    };
    const controller = new PaymentController(
      beans.paymentServices.processPayment,
      orderServiceClient
    );

    const request = new ProcessPaymentRequest(
      "order-test-789",
      new PaymentDetails(75, PaymentMethod.CARD, Currency.AUD)
    );

    const response = await controller.processPayment(request);

    expect(response.status).toBe(PaymentStatus.COMPLETED);
  });

  it("does not notify order-service when payment is rejected", async () => {
    const mockUseCase: ProcessPayment = {
      execute: async () => ({
        type: "PAYMENT_REJECTED",
        reason: "Card declined",
      }),
    };
    const orderServiceClient: OrderServiceClient = {
      markOrderAsPaid: vi.fn().mockResolvedValue({ success: true }),
    };
    const controller = new PaymentController(mockUseCase, orderServiceClient);

    const request = new ProcessPaymentRequest(
      "order-test-999",
      new PaymentDetails(100, PaymentMethod.CARD, Currency.AUD)
    );

    await expect(controller.processPayment(request)).rejects.toThrow();
    expect(orderServiceClient.markOrderAsPaid).not.toHaveBeenCalled();
  });
});
