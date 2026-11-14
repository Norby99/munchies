import { ProcessPaymentResult } from "@main/application/port/inbound/ProcessPayment";

// Kept out of controller.ts: tsoa parses that file to generate the OpenAPI spec and cannot
// handle these utility types.

type Failure = Exclude<ProcessPaymentResult, { type: "SUCCESS" }>;

/** HTTP status of each payment failure. */
const STATUS_BY_FAILURE: Record<Failure["type"], number> = {
  INVALID_REQUEST: 400,
  PAYMENT_REJECTED: 400,
  FORBIDDEN: 403,
  ORDER_NOT_FOUND: 404,
  ALREADY_PAID: 409,
  NOTHING_TO_PAY: 409,
  FAILURE: 500,
  ORDER_UNAVAILABLE: 503,
};

/** A payment failure carrying the HTTP status it maps to. */
export class PaymentError extends Error {
  constructor(
    message: string,
    readonly status: number,
  ) {
    super(message);
  }

  static from(failure: Failure): PaymentError {
    return new PaymentError(failure.reason, STATUS_BY_FAILURE[failure.type]);
  }
}
