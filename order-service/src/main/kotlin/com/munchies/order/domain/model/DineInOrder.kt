package com.munchies.order.domain.model

/**
 * Represents a dine-in order in the system.
 *
 * @property id The unique identifier for the order.
 * @property restaurantId The unique identifier for the restaurant where the order was placed.
 * @property customerId The unique identifier for the customer who placed the order.
 * @property status The current status of the order.
 * @property items A list of items included in the order.
 * @property tableInfo Information about the table where the order will be served.
 */
data class DineInOrder(
  override val id: OrderId,
  override val restaurantId: RestaurantId,
  override val customerId: CustomerId,
  override val status: OrderStatus,
  override val items: List<OrderItem>,
  val tableInfo: TableInfo,
) : Order(id, restaurantId, customerId, status, items) {

  override fun nextStatus(): AdvanceStatusResult {
    val next = when (status) {
      OrderStatus.PENDING -> OrderStatus.PREPARING
      OrderStatus.PREPARING -> OrderStatus.READY
      OrderStatus.READY -> OrderStatus.COMPLETED
      else -> return AdvanceStatusResult.Failure.InvalidTransition
    }
    return AdvanceStatusResult.Success(copy(status = next))
  }

  override fun copyWithStatus(status: OrderStatus) = copy(status = status)
}

/**
 * Represents information about a table in a restaurant.
 *
 * @property tableNumber The number of the table.
 * @property numberOfGuests The number of guests seated at the table.
 */
data class TableInfo(
  val tableNumber: Int,
  val numberOfGuests: Int,
)
