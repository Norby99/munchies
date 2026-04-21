import {
  getIdFromEntityId,
  newUUIDEntityId,
  UUIDEntityId,
} from "../external-modules";

class _PaymentId implements UUIDEntityId {
  constructor(id: string | null) {
    this._id = newUUIDEntityId(id);
  }
  _id: UUIDEntityId;
}

export type PaymentId = InstanceType<typeof _PaymentId>;
export const PaymentId = _PaymentId;
