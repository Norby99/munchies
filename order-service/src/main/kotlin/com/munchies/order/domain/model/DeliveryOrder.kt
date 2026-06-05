package com.munchies.order.domain.model

data class DeliveryOrder(
  override val id: OrderId,
  override val restaurantId: RestaurantId,
  override val customerId: CustomerId,
  override val status: OrderStatus = OrderStatus.PENDING,
  override val items: List<OrderItem>,
  val deliveryInfo: DeliveryInfo,
) : Order(id, restaurantId, customerId, status, items)

data class DeliveryInfo(
  val estimatedDeliveryTime: String,
  val deliveryAddress: String,
  val bellName: String,
  val customerPhone: String,
)
