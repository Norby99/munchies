import { Payment } from "@main/domain/model/Payment";
import { PaymentDocument } from "../document/payment-document";
import { PaymentId } from "@main/domain/model/PaymentId";
import { UUIDEntityId } from "munchies-commons/kotlin/commons-modules";
import {
  Currency,
  PaymentMethod,
  PaymentStatus,
} from "munchies-payment-service-shared/kotlin/payment-modules";

export class PaymentFactory {
  static toDomain(document: PaymentDocument): Payment {
    const status =
      typeof document.status === "string"
        ? PaymentStatus.valueOf(document.status)
        : (document.status ?? PaymentStatus.PENDING);
    const currency =
      typeof document.currency === "string"
        ? Currency.valueOf(document.currency)
        : (document.currency ?? Currency.AUD);
    const method = document.method
      ? typeof document.method === "string"
        ? PaymentMethod.valueOf(document.method)
        : document.method
      : PaymentMethod.CARD;

    return new Payment(
      new PaymentId(document._id ?? document.id),
      status,
      Number(document.amount),
      new UUIDEntityId(document.orderId),
      currency,
      method,
      document.payedAt ? new Date(document.payedAt) : null
    );
  }

  static toDocument(domain: Payment): Record<string, unknown> {
    return {
      _id: domain.id.value,
      status: domain.status.name ?? domain.status.toString(),
      amount: domain.amount,
      orderId: domain.orderId.stringValue(),
      currency: domain.currency.name ?? domain.currency.toString(),
      method: domain.method.name ?? domain.method.toString(),
      payedAt: domain.payedAt ?? null,
    };
  }
}
