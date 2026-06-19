package com.munchies.order.application.port.inbound

import com.munchies.order.application.port.inbound.command.PlaceOrderCommand
import com.munchies.order.infrastructure.adapter.dto.OrderDto

/**
 * Use case for placing an order. This is typically used when a customer wants to create a new
 * order with specific details.
 */
interface PlaceOrder {

  /** Executes the place order use case.
   *
   * @param command The command containing the necessary information to place the order.
   * @return A [Result] indicating the outcome of the operation, which can be either success with
   * the created order details or failure with specific reasons.
   */
  fun execute(command: PlaceOrderCommand): Result

  /**
   * Represents the result of the place order operation. It can be either a success with the
   * created order details or a failure with specific reasons.
   * - `Success`: The order was successfully placed, and the details of the created order are
   *              returned.
   * - `Failure`: The attempt to place the order failed, with specific reasons:
   *  - `InvalidDate`: The provided date for the order is invalid (e.g., in the past).
   *  - `EmptyItems`: The order does not contain any items.
   *  - `InvalidItemQuantity`: One or more items in the order have an invalid quantity.
   */
  sealed interface Result {
    data class Success(val order: OrderDto) : Result
    sealed interface Failure : Result {
      data object InvalidDate : Failure
      data object EmptyItems : Failure
      data object InvalidItemQuantity : Failure
    }
  }
}
