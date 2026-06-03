package com.munchies.order.domain.model

import com.munchies.commons.Entity

data class TakeawayOrder(
  override val id: OrderId,
  val status: OrderStatus = OrderStatus.PENDING,
  val items: List<OrderItem>,
  val takeawayInfo: TakeawayInfo,
) : Entity<OrderId>(id) {
  val type = OrderType.TAKEAWAY
}

data class TakeawayInfo(
  val pickupTime: Long,
  val customerName: String,
)
