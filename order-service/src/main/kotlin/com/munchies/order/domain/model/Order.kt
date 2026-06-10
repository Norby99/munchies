package com.munchies.order.domain.model

import com.munchies.commons.Entity

sealed class Order(
  override val id: OrderId,
  open val restaurantId: RestaurantId,
  open val customerId: CustomerId,
  open val status: OrderStatus,
  open val items: List<OrderItem>,
) : Entity<OrderId>(id) {

  companion object {
    fun validateItems(items: List<OrderItem>): ItemsValidationError? {
      if (items.isEmpty()) return ItemsValidationError.EmptyItems
      if (items.any { !it.isValid() }) return ItemsValidationError.InvalidItemQuantity
      return null
    }
  }

  enum class ItemsValidationError {
    EmptyItems,
    InvalidItemQuantity,
  }
}

@JvmInline value class RestaurantId(val value: String)

@JvmInline value class CustomerId(val value: String)
