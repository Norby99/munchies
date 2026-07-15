package com.munchies.order.application.usecase

import com.munchies.order.application.port.inbound.UpdateOrderItems
import com.munchies.order.application.port.inbound.UpdateOrderItems.Result.Failure.*
import com.munchies.order.application.port.inbound.UpdateOrderItems.Result.Success
import com.munchies.order.application.port.inbound.command.UpdateOrderItemsCommand
import com.munchies.order.domain.model.Order.ItemsValidationError
import com.munchies.order.domain.model.Order.UpdateResult.Failure.InvalidItems
import com.munchies.order.domain.model.OrderItem
import com.munchies.order.domain.ports.OrderRepository

/**
 * Use case implementation for updating the items of an existing order.
 *
 * This class handles the business logic for modifying the items associated with an order.
 * It interacts with the OrderRepository to retrieve and update order data.
 *
 * @property repository The repository used to access and modify order data.
 */
class UpdateOrderItemsUseCase(
  private val repository: OrderRepository,
) : UpdateOrderItems {

  override fun execute(command: UpdateOrderItemsCommand): UpdateOrderItems.Result {
    val order = repository.findById(command.orderId)

    return when {
      order == null -> OrderNotFound
      order.customerId != command.customerId -> Unauthorized
      else -> {
        val items = command.items.map { OrderItem(it.menuItemId, it.quantity) }
        when (val result = order.updateItems(items)) {
          is InvalidItems -> {
            when (result.error) {
              ItemsValidationError.EmptyItems -> EmptyItems
              ItemsValidationError.InvalidItemQuantity -> EmptyItems
            }
          }
          is com.munchies.order.domain.model.Order.UpdateResult.Success -> {
            repository.update(result.order)
            Success
          }
        }
      }
    }
  }
}
