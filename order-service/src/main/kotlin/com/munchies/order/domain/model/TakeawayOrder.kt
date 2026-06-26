package com.munchies.order.domain.model

/**
 * Represents a takeaway order in the system.
 *
 * @property id The unique identifier of the order.
 * @property restaurantId The unique identifier of the restaurant associated with the order.
 * @property customerId The unique identifier of the customer who placed the order.
 * @property status The current status of the order.
 * @property items The list of items included in the order.
 * @property takeawayInfo The takeaway information associated with the order.
 */
data class TakeawayOrder(
  override val id: OrderId,
  override val restaurantId: RestaurantId,
  override val customerId: CustomerId,
  override val status: OrderStatus,
  override val items: List<OrderItem>,
  val takeawayInfo: TakeawayInfo,
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

  override fun copyWithItems(items: List<OrderItem>) = copy(items = items)

  /**
   * Updates the takeaway information of the order.
   *
   * @param pickupTime The new pickup time in milliseconds since epoch.
   * @param customerName The new name of the customer picking up the order.
   * @return An [UpdateResult] indicating success or failure of the update operation.
   */
  fun updateInfo(pickupTime: Long, customerName: String): UpdateResult {
    val newInfo = TakeawayInfo(pickupTime, customerName)
    if (!newInfo.isValidTime()) return UpdateResult.Failure.InvalidDate

    return UpdateResult.Success(copy(takeawayInfo = newInfo))
  }

  /**
   * Represents the result of an update operation on the takeaway order.
   */
  sealed interface UpdateResult {
    data class Success(val order: TakeawayOrder) : UpdateResult
    sealed interface Failure : UpdateResult {
      data object InvalidDate : Failure
    }
  }
}

/**
 * Represents information about a takeaway order.
 *
 * @property pickupTime The time when the order is scheduled for pickup, in milliseconds since epoch.
 * @property customerName The name of the customer picking up the order.
 */
data class TakeawayInfo(
  val pickupTime: Long,
  val customerName: String,
) {

  /**
   * Checks if the pickup time is valid (i.e., in the future).
   *
   * @return True if the pickup time is in the future, false otherwise.
   */
  fun isValidTime(): Boolean = pickupTime > System.currentTimeMillis()
}
