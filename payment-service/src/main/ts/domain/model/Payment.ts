import { PaymentId } from "./PaymentId";
import { UUIDEntityId } from "munchies-commons/kotlin/commons-modules";
import {
  PaymentStatus,
  Currency,
  PaymentMethod,
} from "munchies-payment-service-shared/kotlin/payment-modules";

class _Payment {
  constructor(
    id: PaymentId,
    status: PaymentStatus,
    amount: number,
    orderId: UUIDEntityId,
    currency: Currency,
    method: PaymentMethod = PaymentMethod.CARD,
    payedAt: Date | null = null
  ) {
    this.id = id;
    this.status = status;
    this.amount = amount;
    this.orderId = orderId;
    this.currency = currency;
    this.method = method;
    this.payedAt = payedAt;
  }

  readonly id: PaymentId;
  readonly status: PaymentStatus;
  readonly amount: number;
  readonly orderId: UUIDEntityId;
  readonly currency: Currency;
  readonly method: PaymentMethod;
  readonly payedAt: Date | null;

  static create(
    orderId: UUIDEntityId,
    amount: number,
    currency: Currency,
    method: PaymentMethod
  ): _Payment {
    return new _Payment(
      new PaymentId(),
      PaymentStatus.PENDING,
      amount,
      orderId,
      currency,
      method,
      null
    );
  }

  complete(payedAt: Date = new Date()): _Payment {
    return new _Payment(
      this.id,
      PaymentStatus.COMPLETED,
      this.amount,
      this.orderId,
      this.currency,
      this.method,
      payedAt
    );
  }

  fail(): _Payment {
    return new _Payment(
      this.id,
      PaymentStatus.FAILED,
      this.amount,
      this.orderId,
      this.currency,
      this.method,
      null
    );
  }

  cancel(): _Payment {
    return new _Payment(
      this.id,
      PaymentStatus.CANCELLED,
      this.amount,
      this.orderId,
      this.currency,
      this.method,
      null
    );
  }
}

export const Payment = _Payment;
export type Payment = InstanceType<typeof _Payment>;
