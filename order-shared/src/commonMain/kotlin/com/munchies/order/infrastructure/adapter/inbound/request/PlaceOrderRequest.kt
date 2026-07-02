package com.munchies.order.infrastructure.adapter.inbound.request

import com.munchies.order.infrastructure.adapter.dto.OrderItemDto
import kotlin.js.JsExport
import kotlinx.serialization.Serializable

@JsExport
@Serializable
sealed interface PlaceOrderRequest {
  val restaurantId: String
  val customerId: String
  val items: List<OrderItemDto>
}

@JsExport
@Serializable
data class DeliveryRequest(
  override val restaurantId: String,
  override val customerId: String,
  override val items: List<OrderItemDto>,
  val estimatedDeliveryTime: Long,
  val deliveryAddress: String,
  val bellName: String,
  val customerPhone: String,
) : PlaceOrderRequest

@JsExport
@Serializable
data class TakeawayRequest(
  override val restaurantId: String,
  override val customerId: String,
  override val items: List<OrderItemDto>,
  val pickupTime: Long,
  val customerName: String,
) : PlaceOrderRequest

@JsExport
@Serializable
data class DineInRequest(
  override val restaurantId: String,
  override val customerId: String,
  override val items: List<OrderItemDto>,
  val tableNumber: Int,
  val numberOfGuests: Int,
) : PlaceOrderRequest
