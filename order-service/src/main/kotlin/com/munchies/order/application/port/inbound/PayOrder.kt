package com.munchies.order.application.port.inbound

import com.munchies.order.application.port.inbound.command.PayOrderCommand

/**
 * Use case for paying an order. This is typically used when a customer wants to pay for an
 * existing order.
 */
interface PayOrder {

  /**
   * Executes the pay order use case.
   *
   * @param command The command containing the necessary information to pay for the order.
   * @return A [Result] indicating the outcome of the operation, which can be either success with
   *         the order ID or failure with specific reasons.
   */
  fun execute(command: PayOrderCommand): Result

  /**
   * Represents the result of the pay order operation. It can be either a success with the
   * order ID or a failure with specific reasons.
   * - `Success`: The order was successfully paid.
   * - `Failure`: The attempt to pay for the order failed, with specific reasons:
   *  - `OrderNotFound`: The specified order does not exist.
   *  - `AlreadyPaid`: The specified order has already been paid.
   */
  sealed interface Result {
    data object Success : Result
    sealed interface Failure : Result {
      data object OrderNotFound : Failure
      data object AlreadyPaid : Failure
    }
  }
}
