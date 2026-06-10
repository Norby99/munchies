package com.munchies.order.application.usecase

import com.munchies.order.application.port.inbound.AdvanceOrderStatus
import com.munchies.order.application.port.inbound.command.AdvanceOrderStatusCommand
import com.munchies.order.domain.model.Order
import com.munchies.order.domain.model.OrderId
import com.munchies.order.domain.ports.OrderRepository

class AdvanceOrderStatusUseCase(private val repository: OrderRepository) : AdvanceOrderStatus {

  override fun execute(command: AdvanceOrderStatusCommand): AdvanceOrderStatus.Result {
    val order = repository.findById(OrderId(command.orderId))
      ?: return AdvanceOrderStatus.Result.Failure.OrderNotFound

    return when (val result = order.nextStatus()) {
      is Order.AdvanceStatusResult.Failure.InvalidTransition ->
        AdvanceOrderStatus.Result.Failure.InvalidTransition
      is Order.AdvanceStatusResult.Success -> {
        repository.update(result.order)
        AdvanceOrderStatus.Result.Success
      }
    }
  }
}
