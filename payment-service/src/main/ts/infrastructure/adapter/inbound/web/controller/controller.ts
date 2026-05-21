import { Body, Post, Route, Tags } from "tsoa";

import {
  newId,
  PaymentStatus,
  Currency,
  PaymentResponse,
  PaymentRequest,
  PaymentAPI,
} from "../../../../../domain/external-modules";

/**
 * HTTP controller exposing payment endpoints.
 */
@Route("payments")
@Tags("Payments")
export class PaymentController implements PaymentAPI {
  constructor() {
    console.log("PaymentService constructor called");
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
      newId(),
      PaymentStatus.CANCELLED,
      10,
      Currency.AUD
    );
  }
}
