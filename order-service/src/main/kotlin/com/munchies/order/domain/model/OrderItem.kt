package com.munchies.order.domain.model

data class OrderItem(
  val menuItemId: MenuItemId,
  val name: String,
  val unitPrice: Money,
  val quantity: Int,
  val notes: String? = null
) {
  init { require(quantity > 0) { "Quantity must be positive" } }

  fun subtotal(): Money = Money(unitPrice.amount * quantity.toBigDecimal(), unitPrice.currency)
}
