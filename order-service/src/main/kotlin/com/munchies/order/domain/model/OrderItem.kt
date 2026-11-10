package com.munchies.order.domain.model

/**
 * Represents an item in an order: a menu item, the quantity ordered and its unit price.
 *
 * The unit price is the menu price read from restaurant-service when the item was added to the
 * order, and it is frozen from then on: later menu price changes, or the removal of the item
 * from the menu, do not change what the customer owes for this order.
 *
 * @property menuItemId The unique identifier of the menu item being ordered.
 * @property quantity The quantity of the menu item being ordered.
 * @property unitPrice The price of one unit of the menu item at the time it was ordered.
 */
data class OrderItem(val menuItemId: MenuItemId, val quantity: Int, val unitPrice: Price) {
  fun isValid(): Boolean = quantity > 0

  /** The price of this line: [unitPrice] times [quantity]. */
  val subtotal: Price get() = unitPrice * quantity
}

/**
 * An item as requested by the customer, before it is priced against the restaurant's menus.
 * Commands carry these: the client never decides the price of what it orders.
 *
 * @property menuItemId The unique identifier of the requested menu item.
 * @property quantity The requested quantity.
 */
data class RequestedItem(val menuItemId: MenuItemId, val quantity: Int) {
  fun isValid(): Boolean = quantity > 0

  /** Turns this request into an [OrderItem] priced at [unitPrice]. */
  fun pricedAt(unitPrice: Price): OrderItem = OrderItem(menuItemId, quantity, unitPrice)
}

/**
 * Represents the unique identifier for a menu item.
 *
 * This is an inline value class that wraps a string value representing the menu item ID.
 *
 * @property value The string representation of the menu item ID.
 */
@JvmInline value class MenuItemId(val value: String)

/**
 * An amount of money in euro cents. Integer cents avoid the rounding errors of floating point
 * and match the unit payment-service charges in.
 *
 * @property cents The amount in cents; never negative.
 */
@JvmInline value class Price(val cents: Long) {
  init {
    require(cents >= 0) { "A price cannot be negative" }
  }

  operator fun plus(other: Price): Price = Price(cents + other.cents)

  operator fun times(quantity: Int): Price = Price(cents * quantity)

  companion object {
    val ZERO = Price(0)
  }
}
