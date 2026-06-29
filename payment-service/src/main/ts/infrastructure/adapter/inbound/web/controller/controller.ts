import { Body, Post, Route, Tags } from "tsoa";

import { UUIDEntityId } from "munchies-commons/kotlin/commons-modules";
import {
  ProcessPaymentRequest,
  ProcessPaymentResponse,
  PaymentAPI,
  PaymentStatus,
} from "munchies-payment-service-shared/kotlin/payment-modules";

/**
 * HTTP controller exposing payment endpoints.
 */
@Route("payments")
@Tags("Payments")
export class PaymentController extends PaymentAPI {
  constructor() {
    console.log("PaymentService constructor called");
    super();
  }

  /**
   * Creates a payment for the provided order.
   *
   * @param body Request body containing order and amount information.
   * @returns The created payment details and acceptance status.
   */
  @Post()
  override processPayment(
    @Body()
    request: ProcessPaymentRequest
  ): ProcessPaymentResponse {
    return new ProcessPaymentResponse(
      UUIDEntityId.Companion.newId(),
      PaymentStatus.COMPLETED,
      request.paymentDetails.amount,
      request.paymentDetails.currency
    );
  }
}
