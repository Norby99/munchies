package com.munchies.order.application.port.inbound.command

import com.munchies.order.infrastructure.adapter.dto.OrderItemDto

sealed interface PlaceOrderCommand {
  val restaurantId: String
  val customerId: String
  val items: List<OrderItemDto>

  data class Delivery(
    override val restaurantId: String,
    override val customerId: String,
    override val items: List<OrderItemDto>,
    val estimatedDeliveryTime: Long,
    val deliveryAddress: String,
    val bellName: String,
    val customerPhone: String,
  ) : PlaceOrderCommand

  data class Takeaway(
    override val restaurantId: String,
    override val customerId: String,
    override val items: List<OrderItemDto>,
    val pickupTime: Long,
    val customerName: String,
  ) : PlaceOrderCommand

  data class DineIn(
    override val restaurantId: String,
    override val customerId: String,
    override val items: List<OrderItemDto>,
    val tableNumber: Int,
    val numberOfGuests: Int,
  ) : PlaceOrderCommand
}
