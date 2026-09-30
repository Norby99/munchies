package com.munchies.order.domain.factory

import com.munchies.order.domain.model.OrderEvent

/**
 * Represents the result of an order creation operation.
 *
 * This sealed interface defines the possible outcomes of creating an order.
 * It can either be a success, containing the events that create the order (to be applied and
 * appended to the event store), or a failure, indicating the reason for the failure.
 */
sealed interface OrderCreationResult {
  data class Success(val events: List<OrderEvent>) : OrderCreationResult
  sealed interface Failure : OrderCreationResult {
    data object EmptyItems : Failure
    data object InvalidItemQuantity : Failure
    data object InvalidDate : Failure
  }
}
