package com.munchies.order.application.port.inbound

import com.munchies.order.application.port.inbound.command.AdvanceOrderStatusCommand

/**
 * Use case for advancing the status of an order to the next stage in the order processing workflow.
 *
 * The use case ensures that the status transition is valid based on the current state of the order
 * and handles any necessary business logic associated with the transition, such as notifying
 * relevant parties or updating related entities.
 */
interface AdvanceOrderStatus {

  /**
   * Executes the use case to advance the order status.
   *
   * @param command The command containing the necessary information to identify the order and the
   * desired status transition.
   * @return A [Result] indicating the outcome of the operation, which can be either a success or a
   * failure with specific reasons.
   */
  fun execute(command: AdvanceOrderStatusCommand): Result

  /**
   * Represents the result of attempting to advance the order status.
   *
   * - `Success`: The order status was successfully advanced.
   * - `Failure`: The attempt to advance the order status failed, with specific reasons:
   *   - `OrderNotFound`: No order exists with the provided identifier.
   *   - `InvalidTransition`: The requested status transition is not valid from the current state
   *   of the order.
   */
  sealed interface Result {
    data object Success : Result
    sealed interface Failure : Result {
      data object OrderNotFound : Failure
      data object InvalidTransition : Failure
    }
  }
}
