import { describe, expect, it, beforeEach } from "vitest";
import {
  Currency,
  PaymentStatus,
  newUUIDEntityId,
} from "@main/domain/external-modules";
import { Payment } from "@main/domain/model/Payment";
import { PaymentId } from "@main/domain/model/PaymentId";
import { InMemoryPaymentRepository } from "@main/infrastructure/adapter/outbound/memory/InMemoryPaymentRepository";

describe("InMemoryPaymentRepository", () => {
  let repository: InMemoryPaymentRepository;

  beforeEach(() => {
    repository = new InMemoryPaymentRepository();
  });

  const createPayment = (id: string, amount = 10) =>
    new Payment(
      new PaymentId(id),
      PaymentStatus.PENDING,
      amount,
      newUUIDEntityId(`${id}-order`),
      Currency.AUD,
      null
    );

  it("returns null when a payment does not exist", async () => {
    await expect(
      repository.findById(new PaymentId("missing-payment"))
    ).resolves.toBeNull();
  });

  it("stores and retrieves a saved payment", async () => {
    const payment = createPayment("payment-1", 25);

    await repository.save(payment);

    await expect(repository.findById(payment.id)).resolves.toEqual(payment);
  });

  it("updates an existing payment and returns the updated entity", async () => {
    const original = createPayment("payment-2", 10);
    const updated = createPayment("payment-2", 42);

    await repository.save(original);

    await expect(repository.update(updated)).resolves.toEqual(updated);
    await expect(repository.findById(updated.id)).resolves.toEqual(updated);
  });

  it("rejects updating a payment that was not saved", async () => {
    await expect(
      repository.update(createPayment("missing-payment"))
    ).rejects.toBeUndefined();
  });

  it("deletes an existing payment and returns the removed entity", async () => {
    const payment = createPayment("payment-3", 15);

    await repository.save(payment);

    await expect(repository.delete(payment)).resolves.toEqual(payment);
    await expect(repository.findById(payment.id)).resolves.toBeNull();
  });

  it("rejects deleting a payment that was not saved", async () => {
    await expect(
      repository.delete(createPayment("missing-payment"))
    ).rejects.toBeUndefined();
  });

  it("returns the first matching payment for a predicate", async () => {
    const first = createPayment("payment-4", 20);
    const second = createPayment("payment-5", 35);

    await repository.save(first);
    await repository.save(second);

    await expect(
      repository.findByPredicate((payment) => Number(payment.amount) >= 20)
    ).resolves.toEqual(first);
  });

  it("returns null when no payment matches a predicate", async () => {
    await repository.save(createPayment("payment-6", 18));

    await expect(
      repository.findByPredicate((payment) => Number(payment.amount) > 100)
    ).resolves.toBeNull();
  });
});
