package com.munchies.order.infrastructure.adapter.dto

import kotlinx.serialization.Serializable

/**
 * Data class representing an item in an order.
 *
 * @property menuItemId The unique identifier for the menu item.
 * @property quantity The number of units of the menu item in the order.
 */
@Serializable
data class OrderItemDto(val menuItemId: String, val quantity: Int)
