package com.munchies.order.application.port.inbound

import com.munchies.order.application.port.inbound.command.DiscardOrderCommand

/**
 * Use case for discarding an order. This is typically used when a customer wants to cancel an
 * order that has not yet been processed.
 */
interface DiscardOrder {

  /** Executes the discard order use case.
   *
   * @param command The command containing the necessary information to discard the order.
   * @return A [Result] indicating the outcome of the operation, which can be either success or
   * failure with specific reasons.
   */
  fun execute(command: DiscardOrderCommand): Result

  /**
   * Represents the result of the discard order operation. It can be either a success or a failure
   * with specific reasons.
   * - `Success`: The order was successfully discarded.
   * - `Failure`: The attempt to discard the order failed, with specific reasons:
   *  - `OrderNotFound`: No order exists with the provided identifier.
   *  - `Unauthorized`: The user does not have permission to discard the order.
   *  - `OrderNotCancellable`: The order cannot be discarded because it has already been processed
   *  or is in a state that does not allow cancellation.
   */
  sealed interface Result {
    data object Success : Result
    sealed interface Failure : Result {
      data object OrderNotFound : Failure
      data object Unauthorized : Failure
      data object OrderNotCancellable : Failure
    }
  }
}
