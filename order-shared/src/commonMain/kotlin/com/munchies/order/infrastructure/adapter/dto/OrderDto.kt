package com.munchies.order.infrastructure.adapter.dto

import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data class unifying all order types (Delivery, Takeaway, and Dine-In) into a single representation.
 *
 * @property orderType The type of the order (e.g. Delivery, DineIn etc.)
 * @property orderId The unique identifier for the order.
 * @property restaurantId The unique identifier for the restaurant.
 * @property customerId The unique identifier for the customer.
 * @property status The current status of the order.
 * @property items The list of order items.
 *
 * // Delivery-specific properties
 * @property estimatedDeliveryTime The estimated time for delivery in milliseconds (applicable for Delivery orders).
 * @property deliveryAddress The address where the order will be delivered (applicable for Delivery orders).
 * @property bellName The name associated with the doorbell for delivery (applicable for Delivery orders).
 * @property customerPhone The phone number of the customer for contact during delivery (applicable for Delivery orders).
 *
 * // Takeaway-specific properties
 * @property pickupTime The time when the order will be ready for pickup in milliseconds (applicable for Takeaway orders).
 * @property customerName The name of the customer placing the order (applicable for Takeaway orders).
 *
 * // DineIn-specific properties
 * @property tableNumber The table number where the customer will be seated (applicable for Dine-In orders).
 * @property numberOfGuests The number of guests for the dine-in order (applicable for Dine-In orders).
 */
@JsExport
@Serializable
@SerialName("OrderDto")
data class OrderDto(
  val orderType: OrderType,
  val orderId: String,
  val restaurantId: String,
  val customerId: String,
  val status: String,
  val items: List<OrderItemDto>,

  // Delivery
  val estimatedDeliveryTime: String? = null,
  val deliveryAddress: String? = null,
  val bellName: String? = null,
  val customerPhone: String? = null,

  // Takeaway
  val pickupTime: String? = null,
  val customerName: String? = null,

  // DineIn
  val tableNumber: Int? = null,
  val numberOfGuests: Int? = null,
)
