/**
 * Raised when a reservation, or one of its value objects, would break a domain rule: an
 * impossible time slot, a party of zero guests, a transition the current status does not allow.
 */
export class InvalidReservationError extends Error {
  constructor(message: string) {
    super(message);
    this.name = "InvalidReservationError";
  }
}
