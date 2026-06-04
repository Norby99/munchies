package com.munchies.order.domain.model

data class TakeawayOrder(
  override val id: OrderId,
  override val restaurantId: RestaurantId,
  override val customerId: CustomerId,
  override val status: OrderStatus = OrderStatus.PENDING,
  override val items: List<OrderItem>,
  val takeawayInfo: TakeawayInfo,
) : Order(id, restaurantId, customerId, status, items) {
  val type = OrderType.TAKEAWAY
}

data class TakeawayInfo(
  val pickupTime: Long,
  val customerName: String,
)
