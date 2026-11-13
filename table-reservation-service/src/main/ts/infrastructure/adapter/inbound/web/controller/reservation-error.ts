import { ReservationFailure } from "@main/application/port/inbound/ReservationResult";

// Kept out of controller.ts: tsoa parses that file to generate the OpenAPI spec and cannot
// handle these utility types.

/** HTTP status of each reservation failure. */
const STATUS_BY_FAILURE: Record<ReservationFailure["type"], number> = {
  INVALID_REQUEST: 400,
  FORBIDDEN: 403,
  RESTAURANT_NOT_FOUND: 404,
  RESERVATION_NOT_FOUND: 404,
  NO_TABLE_AVAILABLE: 409,
  NOT_CHANGEABLE: 409,
  RESTAURANT_UNAVAILABLE: 503,
};

/** A reservation failure carrying the HTTP status it maps to. */
export class ReservationError extends Error {
  constructor(
    message: string,
    readonly status: number,
  ) {
    super(message);
  }

  static from(failure: ReservationFailure): ReservationError {
    return new ReservationError(failure.reason, STATUS_BY_FAILURE[failure.type]);
  }
}
