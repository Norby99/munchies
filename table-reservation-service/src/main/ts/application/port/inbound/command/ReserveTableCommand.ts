/**
 * Input of the reserve-table use case.
 *
 * @property start When the customer arrives. The slot length is decided by the service.
 */
export interface ReserveTableCommand {
  restaurantId: string;
  customerId: string;
  start: Date;
  numberOfGuests: number;
}
