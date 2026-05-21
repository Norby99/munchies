import { PaymentId } from "../model/PaymentId";
import { Payment } from "../model/Payment";

export interface PaymentRepository {
  findById(id: PaymentId): Promise<Payment | null>;
  save(entity: Payment): Promise<void>;
  update(entity: Payment): Promise<Payment>;
  delete(entity: Payment): Promise<Payment>;
  findByPredicate(predicate: (e: Payment) => boolean): Promise<Payment | null>;
}
