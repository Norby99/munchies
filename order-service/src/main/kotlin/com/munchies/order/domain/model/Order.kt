package com.munchies.order.domain.model

import com.munchies.commons.Entity

sealed class Order(
  override val id: OrderId,
  open val restaurantId: RestaurantId,
  open val customerId: CustomerId,
  open val status: OrderStatus,
  open val items: List<OrderItem>,
) : Entity<OrderId>(id) {

  abstract fun nextStatus(): AdvanceStatusResult

  fun cancel(): CancelResult {
    if (status != OrderStatus.PENDING) {
      return CancelResult.Failure.InvalidTransition
    }
    return CancelResult.Success(copyWithStatus(OrderStatus.CANCELLED))
  }

  protected abstract fun copyWithStatus(status: OrderStatus): Order

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

  sealed interface AdvanceStatusResult {
    data class Success(val order: Order) : AdvanceStatusResult
    sealed interface Failure : AdvanceStatusResult {
      data object InvalidTransition : Failure
    }
  }

  sealed interface UpdateResult {
    data class Success(val order: Order) : UpdateResult
    sealed interface Failure : UpdateResult {
      data class InvalidItems(val error: ItemsValidationError) : Failure
    }
  }

  sealed interface CancelResult {
    data class Success(val order: Order) : CancelResult
    sealed interface Failure : CancelResult {
      data object InvalidTransition : Failure
    }
  }

  enum class ItemsValidationError {
    EmptyItems,
    InvalidItemQuantity,
  }
}

@JvmInline value class RestaurantId(val value: String)

@JvmInline value class CustomerId(val value: String)
