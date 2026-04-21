import { newUUIDEntityId, UUIDEntityId } from "../external-modules";

export class _PaymentId implements UUIDEntityId {
  constructor(id: string | null) {
    this._id = newUUIDEntityId(id);
  }
  readonly _id: UUIDEntityId;
}

export type PaymentId = InstanceType<typeof _PaymentId>;
export const PaymentId = _PaymentId;
