package com.munchies.order.domain.model

/**
 * Represents a delivery order in the system.
 *
 * @property id The unique identifier of the order.
 * @property restaurantId The unique identifier of the restaurant associated with the order.
 * @property customerId The unique identifier of the customer who placed the order.
 * @property status The current status of the order.
 * @property items The list of items included in the order.
 * @property deliveryInfo The delivery information associated with the order.
 */
data class DeliveryOrder(
  override val id: OrderId,
  override val restaurantId: RestaurantId,
  override val customerId: CustomerId,
  override val status: OrderStatus,
  override val items: List<OrderItem>,
  val deliveryInfo: DeliveryInfo,
) : Order(id, restaurantId, customerId, status, items) {

  override fun nextStatus(): AdvanceStatusResult {
    val next = when (status) {
      OrderStatus.PENDING -> OrderStatus.PREPARING
      OrderStatus.PREPARING -> OrderStatus.READY
      OrderStatus.READY -> OrderStatus.ON_THE_WAY
      OrderStatus.ON_THE_WAY -> OrderStatus.COMPLETED
      else -> return AdvanceStatusResult.Failure.InvalidTransition
    }
    return AdvanceStatusResult.Success(copy(status = next))
  }

  override fun copyWithStatus(status: OrderStatus) = copy(status = status)

  override fun copyWithItems(items: List<OrderItem>) = copy(items = items)

  /**
   * Updates the delivery information of the order.
   *
   * @param estimatedDeliveryTime The new estimated delivery time in milliseconds since epoch.
   * @param deliveryAddress The new delivery address.
   * @param bellName The new bell name for the delivery.
   * @param customerPhone The new customer phone number.
   * @return An [UpdateResult] indicating success or failure of the update operation.
   */
  fun updateInfo(
    estimatedDeliveryTime: Long,
    deliveryAddress: String,
    bellName: String,
    customerPhone: String,
  ): UpdateResult {
    val newInfo = DeliveryInfo(estimatedDeliveryTime, deliveryAddress, bellName, customerPhone)
    if (!newInfo.isValidTime()) return UpdateResult.Failure.InvalidDate

    return UpdateResult.Success(copy(deliveryInfo = newInfo))
  }

  /**
   * Represents the result of an update operation on the delivery order.
   */
  sealed interface UpdateResult {
    data class Success(val order: DeliveryOrder) : UpdateResult
    sealed interface Failure : UpdateResult {
      data object InvalidDate : Failure
    }
  }
}

/**
 * Represents the delivery information associated with a delivery order.
 *
 * @property estimatedDeliveryTime The estimated delivery time in milliseconds since epoch.
 * @property deliveryAddress The address where the order will be delivered.
 * @property bellName The name on the doorbell for the delivery.
 * @property customerPhone The phone number of the customer for contact during delivery.
 */
data class DeliveryInfo(
  val estimatedDeliveryTime: Long,
  val deliveryAddress: String,
  val bellName: String,
  val customerPhone: String,
) {

  /**
   * Checks if the estimated delivery time is valid (i.e., in the future).
   *
   * @return `true` if the estimated delivery time is in the future, `false` otherwise.
   */
  fun isValidTime(): Boolean = estimatedDeliveryTime > System.currentTimeMillis()
}
