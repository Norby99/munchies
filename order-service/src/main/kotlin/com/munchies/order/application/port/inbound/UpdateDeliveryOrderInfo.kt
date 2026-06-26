package com.munchies.order.application.port.inbound

import com.munchies.order.application.port.inbound.command.UpdateDeliveryOrderCommand

/**
 * Use case for updating the delivery information of an order. This is typically used when a
 * customer wants to change the delivery address or time for an existing order.
 */
interface UpdateDeliveryOrderInfo {

  /** Executes the update delivery order information use case.
   *
   * @param command The command containing the necessary information to update the delivery order.
   * @return A [Result] indicating the outcome of the operation, which can be either success or
   * failure with specific reasons.
   */
  fun execute(command: UpdateDeliveryOrderCommand): Result

  /**
   * Represents the result of the update delivery order information operation. It can be either a
   * success or a failure with specific reasons.
   * - `Success`: The delivery information was successfully updated.
   * - `Failure`: The attempt to update the delivery information failed, with specific reasons:
   *  - `OrderNotFound`: No order exists with the provided identifier.
   *  - `Unauthorized`: The user does not have permission to update the delivery information for
   *                    this order.
   *  - `InvalidDate`: The provided delivery date is invalid (e.g., in the past or too soon).
   */
  sealed interface Result {
    data object Success : Result
    sealed interface Failure : Result {
      data object OrderNotFound : Failure
      data object Unauthorized : Failure
      data object InvalidDate : Failure
    }
  }
}
