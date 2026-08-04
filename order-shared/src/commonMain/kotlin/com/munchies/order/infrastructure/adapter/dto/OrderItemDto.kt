package com.munchies.order.infrastructure.adapter.dto

import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data class representing an item in an order.
 *
 * @property menuItemId The unique identifier for the menu item.
 * @property quantity The number of units of the menu item in the order.
 */
@JsExport
@Serializable
@SerialName("OrderItemDto")
data class OrderItemDto(val menuItemId: String, val quantity: Int)
