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

  fun updateItems(newItems: List<OrderItem>): UpdateResult {
    val validationError = validateItems(newItems)
    if (validationError != null) return UpdateResult.Failure.InvalidItems(validationError)

    return when (this) {
      is DeliveryOrder -> UpdateResult.Success(copy(items = newItems))
      is TakeawayOrder -> UpdateResult.Success(copy(items = newItems))
      is DineInOrder -> UpdateResult.Success(copy(items = newItems))
    }
  }

  sealed interface UpdateResult {
    data class Success(val order: Order) : UpdateResult
    sealed interface Failure : UpdateResult {
      data class InvalidItems(val error: ItemsValidationError) : Failure
    }
  }

  enum class ItemsValidationError {
    EmptyItems,
    InvalidItemQuantity,
  }
}

@JvmInline value class RestaurantId(val value: String)

@JvmInline value class CustomerId(val value: String)
