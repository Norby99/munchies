package com.munchies.order.infrastructure.adapter.dto

/**
 * Sealed interface representing the data transfer object for an order.
 */
sealed interface OrderDto {
  val orderId: String
  val restaurantId: String
  val customerId: String
  val status: String
  val items: List<OrderItemDto>

  /**
   * Data class representing a delivery order.
   *
   * @property orderId The unique identifier for the order.
   * @property restaurantId The unique identifier for the restaurant.
   * @property customerId The unique identifier for the customer.
   * @property status The current status of the order.
   * @property items The list of order items.
   * @property estimatedDeliveryTime The estimated time for delivery in milliseconds.
   * @property deliveryAddress The address where the order will be delivered.
   * @property bellName The name associated with the doorbell for delivery.
   * @property customerPhone The phone number of the customer for contact during delivery.
   */
  data class Delivery(
    override val orderId: String,
    override val restaurantId: String,
    override val customerId: String,
    override val status: String,
    override val items: List<OrderItemDto>,
    val estimatedDeliveryTime: Long,
    val deliveryAddress: String,
    val bellName: String,
    val customerPhone: String,
  ) : OrderDto

  /**
   * Data class representing a takeaway order.
   *
   * @property orderId The unique identifier for the order.
   * @property restaurantId The unique identifier for the restaurant.
   * @property customerId The unique identifier for the customer.
   * @property status The current status of the order.
   * @property items The list of order items.
   * @property pickupTime The time when the order will be ready for pickup in milliseconds.
   * @property customerName The name of the customer placing the order.
   */
  data class Takeaway(
    override val orderId: String,
    override val restaurantId: String,
    override val customerId: String,
    override val status: String,
    override val items: List<OrderItemDto>,
    val pickupTime: Long,
    val customerName: String,
  ) : OrderDto

  /**
   * Data class representing a dine-in order.
   *
   * @property orderId The unique identifier for the order.
   * @property restaurantId The unique identifier for the restaurant.
   * @property customerId The unique identifier for the customer.
   * @property status The current status of the order.
   * @property items The list of order items.
   * @property tableNumber The table number where the customer will be seated.
   * @property numberOfGuests The number of guests for the dine-in order.
   */
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
