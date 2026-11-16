import { ErrorResponse } from "munchies-commons/kotlin/commons-modules";

/** Base URL of table-reservation-service, or the error to answer with when it is not set. */
export function reservationServiceUrl(): string | ErrorResponse {
  return (
    process.env.TABLE_RESERVATION_SERVICE_URL ??
    new ErrorResponse("Missing Table Reservation Service URL", 500)
  );
}
