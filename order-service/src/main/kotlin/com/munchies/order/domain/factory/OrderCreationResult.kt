package com.munchies.order.domain.factory

import com.munchies.order.domain.model.Order

/**
 * Represents the result of an order creation operation.
 *
 * This sealed interface defines the possible outcomes of creating an order.
 * It can either be a success, containing the created Order, or a failure, indicating
 * the reason for the failure.
 */
sealed interface OrderCreationResult {
  data class Success(val order: Order) : OrderCreationResult
  sealed interface Failure : OrderCreationResult {
    data object EmptyItems : Failure
    data object InvalidItemQuantity : Failure
    data object InvalidDate : Failure
  }
}
