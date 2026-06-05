package com.munchies.order.infrastructure.adapter.dto

sealed interface OrderDto {
  val orderId: String
  val restaurantId: String
  val customerId: String
  val status: String
  val items: List<OrderItemDto>

  data class Delivery(
    override val orderId: String,
    override val restaurantId: String,
    override val customerId: String,
    override val status: String,
    override val items: List<OrderItemDto>,
    val deliveryAddress: String,
    val bellName: String,
    val customerPhone: String,
  ) : OrderDto

  data class Takeaway(
    override val orderId: String,
    override val restaurantId: String,
    override val customerId: String,
    override val status: String,
    override val items: List<OrderItemDto>,
    val pickupTime: String,
    val customerName: String,
  ) : OrderDto

  data class DineIn(
    override val orderId: String,
    override val restaurantId: String,
    override val customerId: String,
    override val status: String,
    override val items: List<OrderItemDto>,
    val tableNumber: Int,
    val numberOfGuests: Int,
  ) : OrderDto
}
