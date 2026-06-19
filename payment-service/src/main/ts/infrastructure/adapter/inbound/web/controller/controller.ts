import { Body, Post, Route, Tags } from "tsoa";

import {
  PaymentStatus,
  Currency,
  PaymentResponse,
  PaymentRequest,
  PaymentAPI,
} from "../../../../../domain/external-modules";
import { UUIDEntityId } from "munchies-commons/kotlin/commons-modules";

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
  public processPayment(
    @Body()
    request: PaymentRequest
  ): PaymentResponse {
    return new PaymentResponse(
      UUIDEntityId.Companion.newId(),
      PaymentStatus.COMPLETED,
      request.paymentDetails.amount,
      request.paymentDetails.currency
    );
  }
}
