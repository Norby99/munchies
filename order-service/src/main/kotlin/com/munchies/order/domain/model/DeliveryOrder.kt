package com.munchies.order.domain.model

data class DeliveryOrder(
  override val id: OrderId,
  override val restaurantId: String,
  override val customerId: String,
  val status: OrderStatus = OrderStatus.PENDING,
  val items: List<OrderItem>,
  val deliveryInfo: DeliveryInfo,
) : Order(id, restaurantId, customerId) {
  val type = OrderType.DELIVERY
}

data class DeliveryInfo(
  val deliveryAddress: String,
  val bellName: String,
  val customerPhone: String,
)
