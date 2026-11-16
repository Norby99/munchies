package com.munchies.order.infrastructure.adapter.dto

import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data class representing an item in an order.
 *
 * @property menuItemId The unique identifier for the menu item.
 * @property quantity The number of units of the menu item in the order.
 * @property unitPriceCents The price of one unit in euro cents, frozen when the item was ordered.
 * Set by order-service from the restaurant's menu: in requests it is ignored (and may be
 * omitted), so a client can never choose what it pays.
 */
@JsExport
@Serializable
@SerialName("OrderItemDto")
data class OrderItemDto(
  val menuItemId: String,
  val quantity: Int,
  val unitPriceCents: Int = 0,
)
