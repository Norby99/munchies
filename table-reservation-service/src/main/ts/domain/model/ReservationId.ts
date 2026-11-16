import { newId, UUIDEntityId } from "munchies-commons/kotlin/commons-modules";

/** Identifier of a {@link TableReservation}. */
export class ReservationId extends UUIDEntityId {
  constructor(value: string = newId()) {
    super(value);
  }
}
