package com.munchies.order.domain.model

data class DineInOrder(
  override val id: OrderId,
  override val restaurantId: RestaurantId,
  override val customerId: CustomerId,
  override val items: List<OrderItem>,
  val tableInfo: TableInfo,
) : Order(id, restaurantId, customerId, items)

data class TableInfo(
  val tableNumber: Int,
  val numberOfGuests: Int,
)
