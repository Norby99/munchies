package com.munchies.order.domain.model

data class DineInOrder(
  override val id: OrderId,
  override val restaurantId: String,
  override val customerId: String,
  val status: OrderStatus = OrderStatus.PENDING,
  val items: List<OrderItem>,
  val tableInfo: TableInfo,
) : Order(id, restaurantId, customerId) {
  val type = OrderType.DINE_IN
}

data class TableInfo(
  val tableNumber: Int,
  val numberOfGuests: Int,
)
