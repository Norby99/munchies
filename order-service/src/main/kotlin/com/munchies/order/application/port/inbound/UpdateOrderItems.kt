package com.munchies.order.application.port.inbound

import com.munchies.order.application.port.inbound.command.UpdateOrderItemsCommand
import com.munchies.order.domain.model.MenuItemId

/**
 * Use case for updating the items in an existing order. This is typically used when a customer
 * wants to modify their order before it has been processed.
 */
interface UpdateOrderItems {

  /** Executes the update order items use case.
   *
   * @param command The command containing the necessary information to update the order items.
   * @return A [Result] indicating the outcome of the operation, which can be either success or
   * failure with specific reasons.
   */
  fun execute(command: UpdateOrderItemsCommand): Result

  /**
   * Represents the result of the update order items operation. It can be either a success or a
   * failure with specific reasons.
   * - `Success`: The order items were successfully updated.
   * - `Failure`: The attempt to update the order items failed, with specific reasons:
   *  - `OrderNotFound`: No order exists with the provided identifier.
   *  - `Unauthorized`: The user does not have permission to update the order items.
   *  - `EmptyItems`: The provided list of items is empty, which is not allowed for an order.
   *  - `OrderNotModifiable`: The order has left the `PENDING` status, so its items can no longer
   *  change.
   *  - `UnknownItems`: One or more items are not on any menu of the order's restaurant.
   *  - `MenuUnavailable`: restaurant-service could not be asked for the prices.
   */
  sealed interface Result {
    data object Success : Result
    sealed interface Failure : Result {
      data object OrderNotFound : Failure
      data object Unauthorized : Failure
      data object EmptyItems : Failure
      data object OrderNotModifiable : Failure
      data class UnknownItems(val menuItemIds: Set<MenuItemId>) : Failure
      data object MenuUnavailable : Failure
    }
  }
}
