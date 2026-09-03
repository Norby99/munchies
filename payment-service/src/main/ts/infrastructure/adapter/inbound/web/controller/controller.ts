import { Body, Post, Route, Tags, SuccessResponse, Response } from "tsoa";
import {
  ProcessPaymentRequest,
  ProcessPaymentResponse,
} from "munchies-payment-service-shared/kotlin/payment-modules";
import { ProcessPayment } from "@main/application/port/inbound/ProcessPayment";
import { PaymentBeans } from "@main/infrastructure/adapter/inbound/web/config/PaymentBeans";

/**
 * HTTP controller exposing payment endpoints.
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
      case "SUCCESS":
        return result.response;
      case "INVALID_REQUEST":
      case "PAYMENT_REJECTED":
      case "FAILURE":
        throw new Error(result.reason);
    }
  }
}
