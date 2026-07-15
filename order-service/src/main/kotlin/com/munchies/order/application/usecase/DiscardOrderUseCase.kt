package com.munchies.order.application.usecase

import com.munchies.order.application.port.inbound.DiscardOrder
import com.munchies.order.application.port.inbound.DiscardOrder.Result.*
import com.munchies.order.application.port.inbound.DiscardOrder.Result.Failure.*
import com.munchies.order.application.port.inbound.command.DiscardOrderCommand
import com.munchies.order.domain.model.Order
import com.munchies.order.domain.ports.OrderRepository

/**
 * Use case implementation for discarding an order.
 *
 * This class handles the business logic for canceling an order. It interacts with the OrderRepository
 * to retrieve and update order data. The order can only be discarded if it is in a cancellable state.
 *
 * @property repository The repository used to access and modify order data.
 */
class DiscardOrderUseCase(private val repository: OrderRepository) : DiscardOrder {
  override fun execute(command: DiscardOrderCommand): DiscardOrder.Result {
    val order = repository.findById(command.orderId)
    return when {
      order == null -> OrderNotFound
      else -> when (val result = order.cancel()) {
        is Order.CancelResult.Failure.InvalidTransition -> OrderNotCancellable
        is Order.CancelResult.Success -> {
          repository.update(result.order)
          Success
        }
      }
    }
  }
}
