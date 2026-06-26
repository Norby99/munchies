package com.munchies.order.domain.model

import com.munchies.commons.Entity

/**
 * Represents an order in the system, which can be of different types (Delivery, Takeaway, DineIn).
 *
 * @property id The unique identifier for the order.
 * @property restaurantId The unique identifier for the restaurant associated with the order.
 * @property customerId The unique identifier for the customer who placed the order.
 * @property status The current status of the order.
 * @property items The list of items included in the order.
 */
sealed class Order(
  override val id: OrderId,
  open val restaurantId: RestaurantId,
  open val customerId: CustomerId,
  open val status: OrderStatus,
  open val items: List<OrderItem>,
) : Entity<OrderId>(id) {

  /**
   * Advances the status of the order to the next logical state.
   *
   * @return An [AdvanceStatusResult] indicating success or failure of the status advancement.
   */
  abstract fun nextStatus(): AdvanceStatusResult

  /**
   * Cancels the order if it is in a cancellable state.
   *
   * @return A [CancelResult] indicating success or failure of the cancellation.
   */
  fun cancel(): CancelResult {
    if (status != OrderStatus.PENDING) {
      return CancelResult.Failure.InvalidTransition
    }
    return CancelResult.Success(copyWithStatus(OrderStatus.CANCELLED))
  }

  /**
   * Creates a copy of the order with the specified status.
   *
   * @param status The new status for the copied order.
   * @return A new instance of [Order] with the updated status.
   */
  protected abstract fun copyWithStatus(status: OrderStatus): Order

  companion object {
    /**
     * Validates the list of order items.
     *
     * @param items The list of [OrderItem] to validate.
     * @return An [ItemsValidationError] if validation fails, or null if validation succeeds.
     */
    fun validateItems(items: List<OrderItem>): ItemsValidationError? {
      if (items.isEmpty()) return ItemsValidationError.EmptyItems
      if (items.any { !it.isValid() }) return ItemsValidationError.InvalidItemQuantity
      return null
    }
  }

  /**
   * Updates the items in the order after validating them.
   *
   * @param newItems The new list of [OrderItem] to update in the order.
   * @return An [UpdateResult] indicating success or failure of the update operation.
   */
  fun updateItems(newItems: List<OrderItem>): UpdateResult {
    val validationError = validateItems(newItems)
    if (validationError != null) return UpdateResult.Failure.InvalidItems(validationError)

    return when (this) {
      is DeliveryOrder -> UpdateResult.Success(copy(items = newItems))
      is TakeawayOrder -> UpdateResult.Success(copy(items = newItems))
      is DineInOrder -> UpdateResult.Success(copy(items = newItems))
    }
  }

  /**
   * Represents the result of attempting to advance the status of an order.
   */
  sealed interface AdvanceStatusResult {
    data class Success(val order: Order) : AdvanceStatusResult
    sealed interface Failure : AdvanceStatusResult {
      data object InvalidTransition : Failure
    }
  }

  /**
   * Represents the result of attempting to update the items in an order.
   */
  sealed interface UpdateResult {
    data class Success(val order: Order) : UpdateResult
    sealed interface Failure : UpdateResult {
      data class InvalidItems(val error: ItemsValidationError) : Failure
    }
  }

  /**
   * Represents the result of attempting to cancel an order.
   */
  sealed interface CancelResult {
    data class Success(val order: Order) : CancelResult
    sealed interface Failure : CancelResult {
      data object InvalidTransition : Failure
    }
  }

  /**
   * Represents possible validation errors for order items.
   */
  enum class ItemsValidationError {
    EmptyItems,
    InvalidItemQuantity,
  }
}

/**
 * Represents the unique identifier for an order.
 *
 * @property value The string value of the order ID.
 */
@JvmInline value class RestaurantId(val value: String)

/**
 * Represents the unique identifier for a customer.
 *
 * @property value The string value of the customer ID.
 */
@JvmInline value class CustomerId(val value: String)
