import { Body, Post, Route, Tags, SuccessResponse, Response } from "tsoa";
import {
  ProcessPaymentRequest,
  ProcessPaymentResponse,
} from "munchies-payment-service-shared/kotlin/payment-modules";
import { ProcessPayment } from "@main/application/port/inbound/ProcessPayment";
import { OrderServiceClient } from "@main/domain/port/order-service-client";
import { PaymentNotificationPublisher } from "@main/domain/port/payment-notification-publisher";
import { PaymentBeans } from "@main/infrastructure/adapter/inbound/web/config/PaymentBeans";

/**
 * HTTP controller exposing payment endpoints.
 */
@Route("payments")
@Tags("Payments")
export class PaymentController {
  private readonly processPaymentUseCase: ProcessPayment;
  private readonly orderServiceClient: OrderServiceClient;
  private readonly paymentNotificationPublisher: PaymentNotificationPublisher;

  constructor(
    processPaymentUseCase?: ProcessPayment,
    orderServiceClient?: OrderServiceClient,
    paymentNotificationPublisher?: PaymentNotificationPublisher
  ) {
    this.processPaymentUseCase =
      processPaymentUseCase ?? PaymentBeans.getDefaultServices().processPayment;
    this.orderServiceClient =
      orderServiceClient ?? PaymentBeans.getDefaultServices().orderServiceClient;
    this.paymentNotificationPublisher =
      paymentNotificationPublisher ??
      PaymentBeans.getDefaultServices().paymentNotificationPublisher;
  }

  /**
   * Creates and processes a payment for the provided order.
   *
   * On success, order-service is notified over its REST API so the order
   * can be flagged as paid, and a payment-success event is published on
   * Kafka for notification-service to pick up. Both notifications are
   * best-effort: neither order-service nor Kafka being unreachable rolls
   * back the already-completed payment, it is only logged, since
   * compensating this case is not yet handled.
   *
   * @param request Request body containing order and amount information.
   * @returns The created payment details and acceptance status.
   */
  @Post()
  @SuccessResponse("200", "Payment processed successfully")
  @Response("400", "Invalid payment request or payment rejected")
  public async processPayment(
    @Body()
    request: ProcessPaymentRequest
  ): Promise<ProcessPaymentResponse> {
    const result = await this.processPaymentUseCase.execute(request);

    switch (result.type) {
      case "SUCCESS": {
        const notification = await this.orderServiceClient.markOrderAsPaid(
          request.orderId
        );
        if (!notification.success) {
          console.error(
            `Failed to notify order-service that order ${request.orderId} was paid: ` +
              notification.errorMessage
          );
        }

        try {
          await this.paymentNotificationPublisher.publishPaymentSuccess(
            result.payment
          );
        } catch (error: unknown) {
          const message =
            error instanceof Error ? error.message : String(error);
          console.error(
            `Failed to publish payment-success event for order ${request.orderId}: ` +
              message
          );
        }

        return result.response;
      }
      case "INVALID_REQUEST":
      case "PAYMENT_REJECTED":
      case "FAILURE":
        throw new Error(result.reason);
    }
  }
}
