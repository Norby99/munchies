import { getIdFromEntityId } from "@main/domain/external-modules";
import { Payment } from "@main/domain/model/Payment";
import { PaymentId } from "@main/domain/model/PaymentId";
import { PaymentRepository } from "@main/domain/port/payment-repository";
import {
  PaymentDocument,
  PaymentModel,
} from "@main/infrastructure/adapter/outbound/mongo/document/payment-document";
import { PaymentFactory } from "@main/infrastructure/adapter/outbound/mongo/factory/payment-factory";

export class PaymentMongoRepository implements PaymentRepository {
  async findById(id: PaymentId): Promise<Payment | null> {
    const _id = getIdFromEntityId(id);
    const doc: PaymentDocument | null = await PaymentModel.findById(_id);
    return doc ? PaymentFactory.toDomain(doc) : null;
  }

  async save(entity: Payment): Promise<void> {
    const data = PaymentFactory.toDocument(entity);
    await PaymentModel.findByIdAndUpdate(data._id, data, {
      upsert: true,
      new: true,
    });
  }
  async update(entity: Payment): Promise<Payment> {
    const data = PaymentFactory.toDocument(entity);
    const doc: PaymentDocument | null = await PaymentModel.findByIdAndUpdate(
      data._id,
      { $set: data },
      { new: true, runValidators: true }
    );
    return PaymentFactory.toDomain(doc!!);
  }
  async delete(entity: Payment): Promise<Payment> {
    const _id = getIdFromEntityId(entity.id);
    const doc: PaymentDocument | null = await PaymentModel.findByIdAndDelete(
      _id
    );
    if (!doc) throw new Error(`Payment ${_id} not found`);
    return PaymentFactory.toDomain(doc);
  }
  async findByPredicate(
    predicate: (e: Payment) => boolean
  ): Promise<Payment | null> {
    const docs = await PaymentModel.find();
    const payments = docs.map(PaymentFactory.toDomain);
    return payments.find(predicate) ?? null;
  }
}
