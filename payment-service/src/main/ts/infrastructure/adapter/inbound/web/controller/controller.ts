import { Body, Post, Route, Tags, SuccessResponse, Response } from "tsoa";
import {
  ProcessPaymentRequest,
  ProcessPaymentResponse,
} from "munchies-payment-service-shared/kotlin/payment-modules";
import { ProcessPayment } from "@main/application/port/inbound/ProcessPayment";
import { PaymentError } from "./payment-error";

export { PaymentError } from "./payment-error";
import { PaymentBeans } from "@main/infrastructure/adapter/inbound/web/config/PaymentBeans";

/**
 * HTTP controller exposing payment endpoints.
 *
 * It only translates HTTP to and from the application layer: the whole payment workflow,
 * including informing order-service and publishing the payment-success event for
 * notification-service, lives in the {@link ProcessPayment} use case.
 */
@Route("payments")
@Tags("Payments")
export class PaymentController {
  private readonly processPaymentUseCase: ProcessPayment;

  constructor(processPaymentUseCase?: ProcessPayment) {
    this.processPaymentUseCase =
      processPaymentUseCase ?? PaymentBeans.getDefaultServices().processPayment;
  }

  /**
   * Creates and processes a payment for the provided order.
   *
   * @param request The order to pay, the paying customer and the payment method. The amount
   * is the order total read from order-service.
   * @returns The created payment details and acceptance status.
   */
  @Post()
  @SuccessResponse("200", "Payment processed successfully")
  @Response("400", "Invalid payment request or payment rejected")
  @Response("403", "The order belongs to another customer")
  @Response("404", "Order not found")
  @Response("409", "Order already paid, or nothing to pay")
  @Response("503", "order-service unavailable, retry later")
  public async processPayment(
    @Body()
    request: ProcessPaymentRequest
  ): Promise<ProcessPaymentResponse> {
    const result = await this.processPaymentUseCase.execute(request);

    if (result.type === "SUCCESS") return result.response;
    throw PaymentError.from(result);
  }
}
