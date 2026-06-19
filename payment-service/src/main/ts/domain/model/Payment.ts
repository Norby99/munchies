import { PaymentStatus, Currency } from "../external-modules";
import { PaymentId } from "./PaymentId";
import { UUIDEntityId } from "munchies-commons/kotlin/commons-modules";
class _Payment {
  constructor(
    id: PaymentId,
    status: PaymentStatus,
    amount: Number,
    orderId: UUIDEntityId,
    currency: Currency,
    payedAt: Date | null
  ) {
    this.id = id;
    this.status = status;
    this.amount = amount;
    this.orderId = orderId;
    this.currency = currency;
    this.payedAt = payedAt;
  }
  readonly id: PaymentId;
  readonly status: PaymentStatus;
  readonly amount: Number;
  readonly orderId: UUIDEntityId;
  readonly currency: Currency;
  readonly payedAt: Date | null;
}

export const Payment = _Payment;
export type Payment = InstanceType<typeof _Payment>;
