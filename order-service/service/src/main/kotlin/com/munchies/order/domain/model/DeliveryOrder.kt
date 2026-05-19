package com.munchies.order.domain.model

import com.munchies.commons.Entity

data class DeliveryOrder(
  override val id: OrderId,
  val status: OrderStatus,
  val items: List<OrderItem>,
  val deliveryInfo: DeliveryInfo,
) : Entity<OrderId>(id) {
  val type = OrderType.DELIVERY
}

data class DeliveryInfo(
  val deliveryAddress: String,
  val customerName: String,
  val customerPhone: String,
)
