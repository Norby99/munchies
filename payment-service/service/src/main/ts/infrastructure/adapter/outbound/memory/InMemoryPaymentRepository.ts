import { getIdFromEntityId } from "@main/domain/external-modules";
import { Payment } from "@main/domain/model/Payment";
import { PaymentId } from "@main/domain/model/PaymentId";
import { PaymentRepository } from "@main/domain/port/payment-repository";

export class InMemoryPaymentRepository implements PaymentRepository {
  private map: Map<string, Payment> = new Map();

  findById(id: PaymentId): Promise<Payment | null> {
    const payment = this.map.get(getIdFromEntityId(id)) ?? null;
    return Promise.resolve(payment);
  }

  save(entity: Payment): Promise<void> {
    this.map.set(getIdFromEntityId(entity.id), entity);
    return Promise.resolve();
  }

  update(entity: Payment): Promise<Payment> {
    const key = getIdFromEntityId(entity.id);
    if (!this.map.has(key)) return Promise.reject();
    this.map.set(key, entity);
    return Promise.resolve(entity);
  }

  delete(entity: Payment): Promise<Payment> {
    const key = getIdFromEntityId(entity.id);
    const existing = this.map.get(key);
    if (!existing) return Promise.reject();
    this.map.delete(key);
    return Promise.resolve(existing);
  }

  findByPredicate(predicate: (e: Payment) => boolean): Promise<Payment | null> {
    const match = Array.from(this.map.values()).find(predicate) ?? null;
    return Promise.resolve(match);
  }
}
