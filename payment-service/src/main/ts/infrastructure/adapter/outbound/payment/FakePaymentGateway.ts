import { UUIDEntityId, newId } from "munchies-commons/kotlin/commons-modules";
import {
  Currency,
  PaymentMethod,
} from "munchies-payment-service-shared/kotlin/payment-modules";
import {
  PaymentExecutionResult,
  PaymentGateway,
} from "@main/domain/port/payment-gateway";

export interface FakePaymentGatewayOptions {
  simulateFailure?: boolean;
  failureReason?: string;
  artificialDelayMs?: number;
}

/**
 * Mock/Fake implementation of PaymentGateway.
 * Provides faked payment handling for CARD, CASH, MOBILE_PAYMENT, BANK_TRANSFER, and OTHER methods.
 */
export class FakePaymentGateway implements PaymentGateway {
  constructor(private readonly options: FakePaymentGatewayOptions = {}) {}

  async process(
    orderId: UUIDEntityId,
    amount: number,
    currency: Currency,
    method: PaymentMethod
  ): Promise<PaymentExecutionResult> {
    if (this.options.artificialDelayMs && this.options.artificialDelayMs > 0) {
      await new Promise((resolve) =>
        setTimeout(resolve, this.options.artificialDelayMs)
      );
    }

    if (this.options.simulateFailure) {
      return {
        success: false,
        errorMessage:
          this.options.failureReason ?? "Simulated payment processing failure",
      };
    }

    if (amount <= 0) {
      return {
        success: false,
        errorMessage: "Payment amount must be greater than zero",
      };
    }

    const txnId = newId();

    switch (method) {
      case PaymentMethod.CARD:
        return this.processCardPayment(orderId, amount, currency, txnId);
      case PaymentMethod.CASH:
        return this.processCashPayment(orderId, amount, currency, txnId);
      case PaymentMethod.MOBILE_PAYMENT:
        return this.processMobilePayment(orderId, amount, currency, txnId);
      case PaymentMethod.BANK_TRANSFER:
        return this.processBankTransferPayment(orderId, amount, currency, txnId);
      case PaymentMethod.OTHER:
      default:
        return this.processOtherPayment(orderId, amount, currency, txnId);
    }
  }

  private processCardPayment(
    _orderId: UUIDEntityId,
    _amount: number,
    _currency: Currency,
    txnId: string
  ): PaymentExecutionResult {
    return {
      success: true,
      transactionId: `fake_card_txn_${txnId}`,
    };
  }

  private processCashPayment(
    _orderId: UUIDEntityId,
    _amount: number,
    _currency: Currency,
    txnId: string
  ): PaymentExecutionResult {
    return {
      success: true,
      transactionId: `fake_cash_txn_${txnId}`,
    };
  }

  private processMobilePayment(
    _orderId: UUIDEntityId,
    _amount: number,
    _currency: Currency,
    txnId: string
  ): PaymentExecutionResult {
    return {
      success: true,
      transactionId: `fake_mobile_txn_${txnId}`,
    };
  }

  private processBankTransferPayment(
    _orderId: UUIDEntityId,
    _amount: number,
    _currency: Currency,
    txnId: string
  ): PaymentExecutionResult {
    return {
      success: true,
      transactionId: `fake_bank_txn_${txnId}`,
    };
  }

  private processOtherPayment(
    _orderId: UUIDEntityId,
    _amount: number,
    _currency: Currency,
    txnId: string
  ): PaymentExecutionResult {
    return {
      success: true,
      transactionId: `fake_other_txn_${txnId}`,
    };
  }
}
