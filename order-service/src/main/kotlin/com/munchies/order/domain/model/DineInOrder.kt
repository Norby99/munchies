package com.munchies.order.domain.model

import com.munchies.commons.Entity

data class DineInOrder(
  override val id: OrderId,
  val status: OrderStatus = OrderStatus.PENDING,
  val items: List<OrderItem>,
  val tableInfo: TableInfo,
) : Entity<OrderId>(id) {
  val type = OrderType.DINE_IN
}

data class TableInfo(
  val tableNumber: Int,
  val numberOfGuests: Int,
)
