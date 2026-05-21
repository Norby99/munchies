import { Body, Post, Route, Tags } from "tsoa";
/**
 * HTTP controller exposing notification endpoints.
 */
@Route("Table-Reservation")
@Tags("Table-Reservation")
export class TableReservationController {
  constructor() {
    console.log("Table Reservation Service constructor called");
  }

  /**
   * Creates a table reservation.
   */
  @Post()
  public reserveTable() {}
}
