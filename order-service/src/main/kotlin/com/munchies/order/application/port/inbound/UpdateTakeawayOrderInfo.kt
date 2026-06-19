package com.munchies.order.application.port.inbound

import com.munchies.order.application.port.inbound.command.UpdateTakeawayOrderCommand

/**
 * Use case for updating the information of a takeaway order. This is typically used when a
 * customer wants to modify the details of an existing takeaway order, such as changing the
 * pickup time or updating the items in the order.
 */
interface UpdateTakeawayOrderInfo {

  /** Executes the update takeaway order information use case.
   *
   * @param command The command containing the necessary information to update the takeaway order.
   * @return A [Result] indicating the outcome of the operation, which can be either success or
   * failure with specific reasons.
   */
  fun execute(command: UpdateTakeawayOrderCommand): Result

  /**
   * Represents the result of the update takeaway order information operation. It can be either a
   * success or a failure with specific reasons.
   * - `Success`: The takeaway order information was successfully updated.
   * - `Failure`: The attempt to update the takeaway order information failed, with specific
   * reasons:
   *  - `OrderNotFound`: No order exists with the provided identifier.
   *  - `Unauthorized`: The user does not have permission to update the order information.
   *  - `InvalidDate`: The provided date for pickup is invalid (e.g., in the past).
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
