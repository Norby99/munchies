import {
  ProcessPaymentRequest,
  ProcessPaymentResponse,
} from "munchies-payment-service-shared/kotlin/payment-modules";
import { Payment } from "@main/domain/model/Payment";

/**
 * Outcome of a payment attempt. Every failure but FAILURE is decided before any money moves.
 * - ORDER_NOT_FOUND: no such order.
 * - FORBIDDEN: the order belongs to another customer.
 * - ALREADY_PAID: the order has been paid already (e.g. a double submit).
 * - NOTHING_TO_PAY: the order total is zero (e.g. an order placed before items carried prices).
 * - ORDER_UNAVAILABLE: order-service could not be asked about the order.
 */
export type ProcessPaymentResult =
  | { type: "SUCCESS"; payment: Payment; response: ProcessPaymentResponse }
  | { type: "PAYMENT_REJECTED"; reason: string }
  | { type: "INVALID_REQUEST"; reason: string }
  | { type: "ORDER_NOT_FOUND"; reason: string }
  | { type: "FORBIDDEN"; reason: string }
  | { type: "ALREADY_PAID"; reason: string }
  | { type: "NOTHING_TO_PAY"; reason: string }
  | { type: "ORDER_UNAVAILABLE"; reason: string }
  | { type: "FAILURE"; reason: string };

export interface ProcessPayment {
  execute(request: ProcessPaymentRequest): Promise<ProcessPaymentResult>;
}
