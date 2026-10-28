import {
  ProcessPayment,
  ProcessPaymentResult,
} from "@main/application/port/inbound/ProcessPayment";
import { Payment } from "@main/domain/model/Payment";
import { PaymentRepository } from "@main/domain/port/payment-repository";
import { PaymentGateway } from "@main/domain/port/payment-gateway";
import { OrderServiceClient } from "@main/domain/port/order-service-client";
import { PaymentNotificationPublisher } from "@main/domain/port/payment-notification-publisher";
import { Logger } from "@main/domain/port/logger";
import {
  UUIDEntityId,
} from "munchies-commons/kotlin/commons-modules";
import {
  ProcessPaymentRequest,
  ProcessPaymentResponse,
  ProcessPaymentRequestValidator,
  PaymentStatus,
  InvalidInput,
} from "munchies-payment-service-shared/kotlin/payment-modules";

/**
 * Use case processing a payment for an order.
 *
 * On a successful payment, once the completed payment is persisted, it performs the two
 * follow-up steps of the payment workflow, both through outbound ports:
 * - it informs order-service over REST that the order has been paid;
 * - it publishes a payment-success event on Kafka for notification-service (the only
 *   event-driven consumer in the system), carrying everything notification-service needs
 *   (event-carried state transfer), so it never has to call back into payment-service.
 *
 * Both steps are best-effort and independent of each other: the payment is already completed
 * and durable, so a failure of order-service or of the Kafka broker is logged and does not roll
 * the payment back nor change the result returned to the caller (compensation is not handled
 * yet).
 */
export class ProcessPaymentUseCase implements ProcessPayment {
  constructor(
    private readonly paymentRepository: PaymentRepository,
    private readonly paymentGateway: PaymentGateway,
    private readonly orderServiceClient: OrderServiceClient,
    private readonly paymentNotificationPublisher: PaymentNotificationPublisher,
    private readonly logger: Logger,
    private readonly validator: ProcessPaymentRequestValidator = new ProcessPaymentRequestValidator(),
  ) {}

  async execute(request: ProcessPaymentRequest): Promise<ProcessPaymentResult> {
    const validationResult = this.validator.validate(request);
    if (validationResult instanceof InvalidInput) {
      return {
        type: "INVALID_REQUEST",
        reason: validationResult.reason,
      };
    }

    try {
      const orderId = new UUIDEntityId(request.orderId);
      const amount = request.paymentDetails.amount;
      const currency = request.paymentDetails.currency;
      const method = request.paymentDetails.method;

      const payment = Payment.create(orderId, amount, currency, method);

      const gatewayResult = await this.paymentGateway.process(
        orderId,
        amount,
        currency,
        method,
      );

      if (!gatewayResult.success) {
        const failedPayment = payment.fail();
        await this.paymentRepository.save(failedPayment);
        this.logger.warn("Payment rejected by the payment gateway", {
          paymentId: failedPayment.id.value,
          orderId: orderId.value,
          reason: gatewayResult.errorMessage,
        });
        return {
          type: "PAYMENT_REJECTED",
          reason: gatewayResult.errorMessage ?? "Payment authorization failed",
        };
      }

      const completedPayment = payment.complete();
      await this.paymentRepository.save(completedPayment);
      this.logger.info("Payment completed", {
        paymentId: completedPayment.id.value,
        orderId: orderId.value,
        amount,
        currency: String(currency),
        method: String(method),
      });

      // Follow-up steps run only after the completed payment is durable.
      await this.markOrderAsPaid(completedPayment);
      await this.publishPaymentSuccess(completedPayment);

      const response = new ProcessPaymentResponse(
        completedPayment.id.value,
        PaymentStatus.COMPLETED,
        completedPayment.amount,
        completedPayment.currency,
      );

      return {
        type: "SUCCESS",
        payment: completedPayment,
        response,
      };
    } catch (error: unknown) {
      const errorMessage =
        error instanceof Error
          ? error.message
          : "Unknown payment error occurred";
      this.logger.error("Payment processing failed", {
        orderId: request.orderId,
        reason: errorMessage,
      });
      return {
        type: "FAILURE",
        reason: errorMessage,
      };
    }
  }

  /**
   * Informs order-service that the order of [payment] has been paid. Best-effort: a failure is
   * only logged.
   */
  private async markOrderAsPaid(payment: Payment): Promise<void> {
    try {
      const result = await this.orderServiceClient.markOrderAsPaid(
        payment.orderId.value,
      );
      if (!result.success) {
        this.logger.error("Failed to notify order-service that the order was paid", {
          orderId: payment.orderId.value,
          reason: result.errorMessage,
        });
      }
    } catch (error: unknown) {
      this.logger.error("Failed to notify order-service that the order was paid", {
        orderId: payment.orderId.value,
        reason: errorMessageOf(error),
      });
    }
  }

  /**
   * Publishes the payment-success event for notification-service. Best-effort: a failure is
   * only logged, the payment stays completed.
   */
  private async publishPaymentSuccess(payment: Payment): Promise<void> {
    try {
      await this.paymentNotificationPublisher.publishPaymentSuccess(payment);
    } catch (error: unknown) {
      this.logger.error("Failed to publish the payment-success event", {
        orderId: payment.orderId.value,
        reason: errorMessageOf(error),
      });
    }
  }
}

function errorMessageOf(error: unknown): string {
  return error instanceof Error ? error.message : String(error);
}
