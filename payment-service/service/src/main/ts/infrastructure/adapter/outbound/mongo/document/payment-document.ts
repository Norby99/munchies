import { Currency, PaymentStatus } from "@main/domain/external-modules";
import { Schema, model, HydratedDocument } from "mongoose";

interface _PaymentDocument {
  _id: string;
  status: PaymentStatus;
  amount: number;
  orderId: string;
  currency: Currency;
  payedAt: Date | null;
}

const PaymentSchema = new Schema<_PaymentDocument>(
  {
    _id: { type: String, required: true },
    status: {
      type: String,
      required: true,
      enum: Object(PaymentStatus.values()),
    },
    amount: { type: Number, required: true },
    orderId: { type: String, required: true },
    currency: { type: String, required: true, enum: Object(Currency.values()) },
    payedAt: { type: Date, default: null },
  },
  {
    _id: false,
  }
);

export const PaymentModel = model<_PaymentDocument>("Payment", PaymentSchema);
export type PaymentDocument = HydratedDocument<_PaymentDocument>;
