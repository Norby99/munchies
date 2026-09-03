package com.munchies.order.application.usecase

import com.munchies.order.application.port.inbound.PayOrder
import com.munchies.order.application.port.inbound.command.PayOrderCommand
import com.munchies.order.domain.model.Order
import com.munchies.order.domain.ports.OrderRepository

class PayOrderUseCase(private val repository: OrderRepository) : PayOrder {
  override fun execute(command: PayOrderCommand): PayOrder.Result {
    val order = repository.findById(command.orderId)
      ?: return PayOrder.Result.Failure.OrderNotFound

    return when (val result = order.pay()) {
      is Order.PayResult.Failure.AlreadyPaid ->
        PayOrder.Result.Failure.AlreadyPaid
      is Order.PayResult.Success -> {
        repository.update(result.order)
        PayOrder.Result.Success
      }
    }
  }
}
