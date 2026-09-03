import {
  ProcessPayment,
  ProcessPaymentResult,
} from "@main/application/port/inbound/ProcessPayment";
import { Payment } from "@main/domain/model/Payment";
import { PaymentRepository } from "@main/domain/port/payment-repository";
import { PaymentGateway } from "@main/domain/port/payment-gateway";
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

export class ProcessPaymentUseCase implements ProcessPayment {
  constructor(
    private readonly paymentRepository: PaymentRepository,
    private readonly paymentGateway: PaymentGateway,
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
        return {
          type: "PAYMENT_REJECTED",
          reason: gatewayResult.errorMessage ?? "Payment authorization failed",
        };
      }

      const completedPayment = payment.complete();
      await this.paymentRepository.save(completedPayment);

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
      return {
        type: "FAILURE",
        reason: errorMessage,
      };
    }
  }
}
