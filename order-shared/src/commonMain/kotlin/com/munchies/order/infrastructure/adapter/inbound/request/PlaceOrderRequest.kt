package com.munchies.order.infrastructure.adapter.inbound.request

import com.munchies.order.infrastructure.adapter.dto.OrderItemDto
import com.munchies.order.infrastructure.adapter.dto.OrderType
import kotlin.js.JsExport
import kotlinx.serialization.Serializable

// TODO: ricordarsi di validare i campi null

@JsExport
@Serializable
data class PlaceOrderRequest(
  val restaurantId: String,
  val customerId: String,
  val items: List<OrderItemDto>,
  val orderType: OrderType,

  // Delivery
  val estimatedDeliveryTime: Long? = null,
  val deliveryAddress: String? = null,
  val bellName: String? = null,
  val customerPhone: String? = null,

  // Takeaway
  val pickupTime: Long? = null,
  val customerName: String? = null,

  // DineIn
  val tableNumber: Int? = null,
  val numberOfGuests: Int? = null,
)
