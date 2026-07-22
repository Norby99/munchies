package com.munchies.order.infrastructure.adapter.inbound.request

import com.munchies.order.infrastructure.adapter.dto.OrderItemDto
import com.munchies.order.infrastructure.adapter.dto.OrderType
import kotlin.js.JsExport
import kotlinx.serialization.Serializable

// TODO: ricordarsi di validare i campi null

/**
 * Request data class for placing
 * an order with various order types (Delivery, Takeaway, DineIn).
 *
 * @param restaurantId The unique identifier of the restaurant where the order is being placed.
 * @param customerId The unique identifier of the customer placing the order.
 * @param items The list of order items being ordered.
 * @param orderType The type of the order (Delivery, Takeaway, DineIn).
 * @param estimatedDeliveryTime The estimated delivery time for Delivery orders (optional).
 * @param deliveryAddress The delivery address for Delivery orders (optional).
 * @param bellName The name on the doorbell for Delivery orders (optional).
 * @param customerPhone The phone number of the customer for Delivery orders (optional).
 * @param pickupTime The pickup time for Takeaway orders (optional).
 * @param customerName The name of the customer for Takeaway orders (optional).
 * @param tableNumber The table number for DineIn orders (optional).
 * @param numberOfGuests The number of guests for DineIn orders (optional).
 */
@JsExport
@Serializable
data class PlaceOrderRequest(
  val restaurantId: String,
  val customerId: String,
  val items: List<OrderItemDto>,
  val orderType: OrderType,

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
