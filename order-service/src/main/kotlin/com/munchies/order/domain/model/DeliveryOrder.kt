package com.munchies.order.domain.model

data class DeliveryOrder(
  override val id: OrderId,
  override val restaurantId: RestaurantId,
  override val customerId: CustomerId,
  override val status: OrderStatus,
  override val items: List<OrderItem>,
  val deliveryInfo: DeliveryInfo,
) : Order(id, restaurantId, customerId, status, items) {

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

  sealed interface UpdateResult {
    data class Success(val order: DeliveryOrder) : UpdateResult
    sealed interface Failure : UpdateResult {
      data object InvalidDate : Failure
    }
  }
}

data class DeliveryInfo(
  val estimatedDeliveryTime: Long,
  val deliveryAddress: String,
  val bellName: String,
  val customerPhone: String,
) {
  fun isValidTime(): Boolean = estimatedDeliveryTime > System.currentTimeMillis()
}
