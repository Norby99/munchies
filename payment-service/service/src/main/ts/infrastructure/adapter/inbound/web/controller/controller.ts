import { Body, Post, Route, Tags } from "tsoa";

/**
 * Represents the payload required to create a payment.
 */
interface PaymentRequest {
  /**
   * Unique identifier of the order to be paid.
   */
  orderId: string;

  /**
   * Payment amount expressed in cents.
   */
  amountCents: number;
}

/**
 * Represents the result of a payment creation request.
 */
interface PaymentResponse {
  /**
   * Unique identifier assigned to the created payment.
   */
  paymentId: string;

  /**
   * Indicates whether the payment was accepted.
   */
  accepted: boolean;
}

/**
 * HTTP controller exposing payment endpoints.
 */
@Route("payments")
@Tags("Payments")
export class PaymentController {
  /**
   * Creates a payment for the provided order.
   *
   * @param body Request body containing order and amount information.
   * @returns The created payment details and acceptance status.
   */
  @Post()
  public async createPayment(
    @Body() body: PaymentRequest
  ): Promise<PaymentResponse> {
    return { paymentId: "p-123", accepted: true };
  }
}
