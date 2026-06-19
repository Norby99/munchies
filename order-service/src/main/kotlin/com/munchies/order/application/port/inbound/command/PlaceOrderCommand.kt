package com.munchies.order.application.port.inbound.command

import com.munchies.order.domain.model.CustomerId
import com.munchies.order.domain.model.OrderItem
import com.munchies.order.domain.model.RestaurantId

/**
 * Command to place an order in the system.
 *
 * This sealed interface defines the structure of the command for placing an order,
 * which can be of three types: Delivery, Takeaway, or DineIn. Each type has its own
 * specific fields relevant to the order type.
 */
sealed interface PlaceOrderCommand {
  val restaurantId: RestaurantId
  val customerId: CustomerId
  val items: List<OrderItem>

  /** Represents a delivery order with specific fields for delivery details. */
  data class Delivery(
    override val restaurantId: RestaurantId,
    override val customerId: CustomerId,
    override val items: List<OrderItem>,
    val estimatedDeliveryTime: Long,
    val deliveryAddress: String,
    val bellName: String,
    val customerPhone: String,
  ) : PlaceOrderCommand

  /** Represents a takeaway order with specific fields for pickup details. */
  data class Takeaway(
    override val restaurantId: RestaurantId,
    override val customerId: CustomerId,
    override val items: List<OrderItem>,
    val pickupTime: Long,
    val customerName: String,
  ) : PlaceOrderCommand

  /** Represents a dine-in order with specific fields for table details. */
  data class DineIn(
    override val restaurantId: RestaurantId,
    override val customerId: CustomerId,
    override val items: List<OrderItem>,
    val tableNumber: Int,
    val numberOfGuests: Int,
  ) : PlaceOrderCommand
}
