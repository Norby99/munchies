/**
 * Input of the list-reservations use case. At least one of `customerId` and `restaurantId` is
 * required.
 *
 * @property customerId Only the reservations of this customer.
 * @property restaurantId Only the reservations at this restaurant.
 * @property managerId The manager asking on behalf of a restaurant: when given, `restaurantId`
 * is required and must be a restaurant they manage.
 */
export interface GetTableReservationsCommand {
  customerId?: string;
  restaurantId?: string;
  managerId?: string;
}
