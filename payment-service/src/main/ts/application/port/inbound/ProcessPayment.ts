import {
  ProcessPaymentRequest,
  ProcessPaymentResponse,
} from "munchies-payment-service-shared/kotlin/payment-modules";
import { Payment } from "@main/domain/model/Payment";

export type ProcessPaymentResult =
  | { type: "SUCCESS"; payment: Payment; response: ProcessPaymentResponse }
  | { type: "PAYMENT_REJECTED"; reason: string }
  | { type: "INVALID_REQUEST"; reason: string }
  | { type: "FAILURE"; reason: string };

export interface ProcessPayment {
  execute(request: ProcessPaymentRequest): Promise<ProcessPaymentResult>;
}
