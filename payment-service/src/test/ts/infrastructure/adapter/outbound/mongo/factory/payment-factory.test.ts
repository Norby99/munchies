import { describe, expect, it } from "vitest";
import { PaymentFactory } from "@main/infrastructure/adapter/outbound/mongo/factory/payment-factory";
import { PaymentDocument } from "@main/infrastructure/adapter/outbound/mongo/document/payment-document";
import {
  Currency,
  PaymentMethod,
  PaymentStatus,
} from "munchies-payment-service-shared/kotlin/payment-modules";

/** Minimal helper to build a raw Mongoose-like document */
const baseDocument = (): Partial<PaymentDocument> & {
  _id: string;
  orderId: string;
  amount: number;
} => ({
  _id: "pay-001",
  status: PaymentStatus.PENDING,
  amount: 150,
  orderId: "order-abc",
  currency: Currency.EUR,
  method: PaymentMethod.CARD,
  payedAt: null,
});

describe("PaymentFactory", () => {
  // ── toDomain ──────────────────────────────────────────────────────────────

  describe("toDomain", () => {
    it("maps a document with enum values for status, currency, and method", () => {
      const doc = baseDocument();
      const payment = PaymentFactory.toDomain(doc as PaymentDocument);

      expect(payment.id.value).toBe("pay-001");
      expect(payment.status).toBe(PaymentStatus.PENDING);
      expect(payment.amount).toBe(150);
      expect(payment.orderId.stringValue()).toBe("order-abc");
      expect(payment.currency).toBe(Currency.EUR);
      expect(payment.method).toBe(PaymentMethod.CARD);
      expect(payment.payedAt).toBeNull();
    });

    it("maps a document with string values for status, currency, and method", () => {
      const doc = {
        ...baseDocument(),
        status: "COMPLETED" as unknown as PaymentStatus,
        currency: "AUD" as unknown as Currency,
        method: "CASH" as unknown as PaymentMethod,
      };
      const payment = PaymentFactory.toDomain(doc as PaymentDocument);

      expect(payment.status).toBe(PaymentStatus.valueOf("COMPLETED"));
      expect(payment.currency).toBe(Currency.valueOf("AUD"));
      expect(payment.method).toBe(PaymentMethod.valueOf("CASH"));
    });

    it("defaults status to PENDING when status is null/undefined", () => {
      const doc = { ...baseDocument(), status: null as unknown as PaymentStatus };
      const payment = PaymentFactory.toDomain(doc as PaymentDocument);

      expect(payment.status).toBe(PaymentStatus.PENDING);
    });

    it("defaults currency to AUD when currency is null/undefined", () => {
      const doc = { ...baseDocument(), currency: null as unknown as Currency };
      const payment = PaymentFactory.toDomain(doc as PaymentDocument);

      expect(payment.currency).toBe(Currency.AUD);
    });

    it("defaults method to CARD when method is null/undefined", () => {
      const doc = { ...baseDocument(), method: null as unknown as PaymentMethod };
      const payment = PaymentFactory.toDomain(doc as PaymentDocument);

      expect(payment.method).toBe(PaymentMethod.CARD);
    });

    it("parses payedAt as a Date when present", () => {
      const date = new Date("2025-01-15T10:00:00.000Z");
      const doc = { ...baseDocument(), payedAt: date.toISOString() as unknown as Date };
      const payment = PaymentFactory.toDomain(doc as PaymentDocument);

      expect(payment.payedAt).toEqual(new Date(date.toISOString()));
    });

    it("falls back to document.id when _id is absent", () => {
      const doc = { ...baseDocument() };
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      delete (doc as any)._id;
      (doc as unknown as { id: string }).id = "fallback-id";

      const payment = PaymentFactory.toDomain(doc as unknown as PaymentDocument);
      expect(payment.id.value).toBe("fallback-id");
    });
  });

  // ── toDocument ────────────────────────────────────────────────────────────

  describe("toDocument", () => {
    it("converts a domain Payment to a plain document record", () => {
      const doc = baseDocument();
      const payment = PaymentFactory.toDomain(doc as PaymentDocument);
      const record = PaymentFactory.toDocument(payment);

      expect(record._id).toBe("pay-001");
      expect(record.orderId).toBe("order-abc");
      expect(record.amount).toBe(150);
      expect(record.payedAt).toBeNull();
    });

    it("serialises status, currency, and method as their name strings", () => {
      const doc = {
        ...baseDocument(),
        status: PaymentStatus.COMPLETED,
        currency: Currency.USD,
        method: PaymentMethod.BANK_TRANSFER,
        payedAt: new Date("2025-03-01"),
      };
      const payment = PaymentFactory.toDomain(doc as PaymentDocument);
      const record = PaymentFactory.toDocument(payment);

      expect(typeof record.status).toBe("string");
      expect(typeof record.currency).toBe("string");
      expect(typeof record.method).toBe("string");
      expect(record.payedAt).toBeInstanceOf(Date);
    });

    it("produces a round-trip consistent document", () => {
      const original = baseDocument();
      const payment = PaymentFactory.toDomain(original as PaymentDocument);
      const record = PaymentFactory.toDocument(payment);

      // Re-map back to domain and verify structural equality
      const roundTripped = PaymentFactory.toDomain(
        record as unknown as PaymentDocument
      );
      expect(roundTripped.id.value).toBe(payment.id.value);
      expect(roundTripped.amount).toBe(payment.amount);
      expect(roundTripped.orderId.stringValue()).toBe(payment.orderId.stringValue());
    });
  });
});
