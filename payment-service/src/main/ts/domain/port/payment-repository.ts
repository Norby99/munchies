import { PaymentId } from "../model/PaymentId";
import { Payment } from "../model/Payment";
import { UUIDEntityId } from "munchies-commons/kotlin/commons-modules";

export interface PaymentRepository {
  findById(id: PaymentId): Promise<Payment | null>;
  findByOrderId(orderId: UUIDEntityId): Promise<Payment | null>;
  save(entity: Payment): Promise<void>;
  update(entity: Payment): Promise<Payment>;
  delete(entity: Payment): Promise<Payment>;
  findByPredicate(predicate: (e: Payment) => boolean): Promise<Payment | null>;
}
