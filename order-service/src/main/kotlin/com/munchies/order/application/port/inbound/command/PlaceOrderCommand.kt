package com.munchies.order.application.port.inbound.command

import com.munchies.order.domain.model.CustomerId
import com.munchies.order.domain.model.OrderItem
import com.munchies.order.domain.model.RestaurantId

sealed interface PlaceOrderCommand {
  val restaurantId: RestaurantId
  val customerId: CustomerId
  val items: List<OrderItem>

  data class Delivery(
    override val restaurantId: RestaurantId,
    override val customerId: CustomerId,
    override val items: List<OrderItem>,
    val estimatedDeliveryTime: Long,
    val deliveryAddress: String,
    val bellName: String,
    val customerPhone: String,
  ) : PlaceOrderCommand

  data class Takeaway(
    override val restaurantId: RestaurantId,
    override val customerId: CustomerId,
    override val items: List<OrderItem>,
    val pickupTime: Long,
    val customerName: String,
  ) : PlaceOrderCommand

  data class DineIn(
    override val restaurantId: RestaurantId,
    override val customerId: CustomerId,
    override val items: List<OrderItem>,
    val tableNumber: Int,
    val numberOfGuests: Int,
  ) : PlaceOrderCommand
}
