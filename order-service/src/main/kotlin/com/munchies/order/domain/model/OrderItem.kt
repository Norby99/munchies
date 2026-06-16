package com.munchies.order.domain.model

/**
 * Represents an item in an order, consisting of a menu item ID and the quantity ordered.
 *
 * @property menuItemId The unique identifier of the menu item being ordered.
 * @property quantity The quantity of the menu item being ordered.
 */
data class OrderItem(val menuItemId: MenuItemId, val quantity: Int) {
  fun isValid(): Boolean = quantity > 0
}

@JvmInline value class MenuItemId(val value: String)
