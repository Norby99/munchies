import { beforeEach, describe, expect, it } from "vitest";
import { InMemoryPaymentRepository } from "@main/infrastructure/adapter/outbound/memory/InMemoryPaymentRepository";
import { Payment } from "@main/domain/model/Payment";
import { PaymentId } from "@main/domain/model/PaymentId";
import { UUIDEntityId } from "munchies-commons/kotlin/commons-modules";
import {
  Currency,
  PaymentMethod,
  PaymentStatus,
} from "munchies-payment-service-shared/kotlin/payment-modules";

describe("InMemoryPaymentRepository", () => {
  let repository: InMemoryPaymentRepository;

  const makePayment = (id: string, orderId: string, amount = 100) =>
    new Payment(
      new PaymentId(id),
      PaymentStatus.PENDING,
      amount,
      new UUIDEntityId(orderId),
      Currency.EUR,
      PaymentMethod.CARD,
      null
    );

  beforeEach(() => {
    repository = new InMemoryPaymentRepository();
  });

  // ── findById ─────────────────────────────────────────────────────────────

  describe("findById", () => {
    it("returns the payment when it exists", async () => {
      const payment = makePayment("id-1", "order-1");
      await repository.save(payment);

      await expect(
        repository.findById(new PaymentId("id-1"))
      ).resolves.toEqual(payment);
    });

    it("returns null when no payment matches the id", async () => {
      await expect(
        repository.findById(new PaymentId("missing"))
      ).resolves.toBeNull();
    });
  });

  // ── findByOrderId ─────────────────────────────────────────────────────────

  describe("findByOrderId", () => {
    it("returns the payment when the orderId matches", async () => {
      const payment = makePayment("id-2", "order-abc");
      await repository.save(payment);

      await expect(
        repository.findByOrderId(new UUIDEntityId("order-abc"))
      ).resolves.toEqual(payment);
    });

    it("returns null when no payment matches the orderId", async () => {
      await expect(
        repository.findByOrderId(new UUIDEntityId("order-xyz"))
      ).resolves.toBeNull();
    });
  });

  // ── save ─────────────────────────────────────────────────────────────────

  describe("save", () => {
    it("persists the payment so it can be retrieved afterwards", async () => {
      const payment = makePayment("id-3", "order-3");

      await expect(repository.save(payment)).resolves.toBeUndefined();
      await expect(
        repository.findById(new PaymentId("id-3"))
      ).resolves.toEqual(payment);
    });

    it("overwrites an existing entry when saved with the same id", async () => {
      const original = makePayment("id-4", "order-4", 50);
      const updated = makePayment("id-4", "order-4", 200);

      await repository.save(original);
      await repository.save(updated);

      await expect(
        repository.findById(new PaymentId("id-4"))
      ).resolves.toEqual(updated);
    });
  });

  // ── update ────────────────────────────────────────────────────────────────

  describe("update", () => {
    it("updates an existing payment and returns the new value", async () => {
      const original = makePayment("id-5", "order-5", 75);
      await repository.save(original);

      const updated = new Payment(
        new PaymentId("id-5"),
        PaymentStatus.COMPLETED,
        75,
        new UUIDEntityId("order-5"),
        Currency.EUR,
        PaymentMethod.CARD,
        new Date()
      );

      await expect(repository.update(updated)).resolves.toEqual(updated);
      await expect(
        repository.findById(new PaymentId("id-5"))
      ).resolves.toEqual(updated);
    });
  });

  // ── delete ────────────────────────────────────────────────────────────────

  describe("delete", () => {
    it("removes the payment and returns the deleted entity", async () => {
      const payment = makePayment("id-6", "order-6");
      await repository.save(payment);

      await expect(repository.delete(payment)).resolves.toEqual(payment);
      await expect(
        repository.findById(new PaymentId("id-6"))
      ).resolves.toBeNull();
    });

 });

  // ── findByPredicate ──────────────────────────────────────────────────────

  describe("findByPredicate", () => {
    it("returns the first payment matching the predicate", async () => {
      const cheap = makePayment("id-7", "order-7", 10);
      const expensive = makePayment("id-8", "order-8", 500);
      await repository.save(cheap);
      await repository.save(expensive);

      const result = await repository.findByPredicate(
        (p) => p.amount >= 500
      );

      expect(result).toEqual(expensive);
    });

    it("returns null when no payment matches the predicate", async () => {
      const payment = makePayment("id-9", "order-9", 20);
      await repository.save(payment);

      await expect(
        repository.findByPredicate((p) => p.amount > 9999)
      ).resolves.toBeNull();
    });

    it("returns null when the repository is empty", async () => {
      await expect(
        repository.findByPredicate(() => true)
      ).resolves.toBeNull();
    });
  });
});
