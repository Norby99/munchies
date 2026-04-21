import { Payment } from "@main/domain/model/Payment";
import { PaymentDocument } from "../document/payment-document";
import { PaymentId } from "@main/domain/model/PaymentId";
import {
  getIdFromEntityId,
  newUUIDEntityId,
} from "@main/domain/external-modules";

export class PaymentFactory {
  static toDomain(document: PaymentDocument): Payment {
    return new Payment(
      new PaymentId(document.id),
      document.status,
      document.amount,
      newUUIDEntityId(document.orderId),
      document.currency,
      document.payedAt
    );
  }

  static toDocument(domain: Payment): Record<string, unknown> {
    return {
      _id: getIdFromEntityId(domain.id),
      status: domain.status,
      amount: domain.amount,
      orderId: getIdFromEntityId(domain.orderId),
      currency: domain.currency,
      payedAt: domain.payedAt ?? null,
    };
  }
}
