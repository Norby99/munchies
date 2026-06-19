import { Payment } from "@main/domain/model/Payment";
import { PaymentDocument } from "../document/payment-document";
import { PaymentId } from "@main/domain/model/PaymentId";
import { UUIDEntityId } from "munchies-commons/kotlin/commons-modules";
export class PaymentFactory {
  static toDomain(document: PaymentDocument): Payment {
    return new Payment(
      new PaymentId(document.id),
      document.status,
      document.amount,
      new UUIDEntityId(document.orderId),
      document.currency,
      document.payedAt
    );
  }

  static toDocument(domain: Payment): Record<string, unknown> {
    return {
      _id: domain.id.value,
      status: domain.status,
      amount: domain.amount,
      orderId: domain.orderId.stringValue(),
      currency: domain.currency,
      payedAt: domain.payedAt ?? null,
    };
  }
}
