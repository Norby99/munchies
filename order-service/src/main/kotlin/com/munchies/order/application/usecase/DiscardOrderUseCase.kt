package com.munchies.order.application.usecase

import com.munchies.order.application.port.inbound.DiscardOrder
import com.munchies.order.application.port.inbound.DiscardOrder.Result.*
import com.munchies.order.application.port.inbound.DiscardOrder.Result.Failure.*
import com.munchies.order.application.port.inbound.command.DiscardOrderCommand
import com.munchies.order.domain.model.Order
import com.munchies.order.domain.model.OrderId
import com.munchies.order.domain.ports.OrderRepository

class DiscardOrderUseCase(private val repository: OrderRepository) : DiscardOrder {
  override fun execute(command: DiscardOrderCommand): DiscardOrder.Result {
    val order = repository.findById(OrderId(command.orderId))
    return when {
      order == null -> OrderNotFound
      order.customerId.value != command.customerId -> Unauthorized
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
