import { newUUIDEntityId, UUIDEntityId } from "../external-modules";

export type PaymentId = UUIDEntityId;

export const PaymentId = new Proxy(class {}, {
  construct(target, args) {
    return newUUIDEntityId(args[0]);
  },
}) as unknown as new (id: string | null) => PaymentId;
