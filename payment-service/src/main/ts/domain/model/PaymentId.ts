import { newId, UUIDEntityId } from "munchies-commons/kotlin/commons-modules";
export class PaymentId extends UUIDEntityId {
  constructor(value: string = newId()) {
    super(value);
  }
}
