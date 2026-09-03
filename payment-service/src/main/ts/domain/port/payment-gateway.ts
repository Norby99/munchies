import { UUIDEntityId } from "munchies-commons/kotlin/commons-modules";
import {
  Currency,
  PaymentMethod,
} from "munchies-payment-service-shared/kotlin/payment-modules";

export interface PaymentExecutionResult {
  success: boolean;
  transactionId?: string;
  errorMessage?: string;
}

export interface PaymentGateway {
  process(
    orderId: UUIDEntityId,
    amount: number,
    currency: Currency,
    method: PaymentMethod
  ): Promise<PaymentExecutionResult>;
}
