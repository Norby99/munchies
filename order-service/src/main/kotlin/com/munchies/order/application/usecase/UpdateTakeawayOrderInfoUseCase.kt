package com.munchies.order.application.usecase

import com.munchies.order.application.port.inbound.UpdateTakeawayOrderInfo
import com.munchies.order.application.port.inbound.UpdateTakeawayOrderInfo.Result.Failure.*
import com.munchies.order.application.port.inbound.UpdateTakeawayOrderInfo.Result.Success
import com.munchies.order.application.port.inbound.command.UpdateTakeawayOrderCommand
import com.munchies.order.domain.model.OrderId
import com.munchies.order.domain.model.TakeawayOrder
import com.munchies.order.domain.ports.OrderRepository

class UpdateTakeawayOrderInfoUseCase(
  private val repository: OrderRepository,
) : UpdateTakeawayOrderInfo {

  override fun execute(command: UpdateTakeawayOrderCommand): UpdateTakeawayOrderInfo.Result {
    val order = repository.findById(OrderId(command.orderId))

    return when {
      order == null -> OrderNotFound
      order.customerId.value != command.customerId -> Unauthorized
      order !is TakeawayOrder -> OrderNotFound
      else -> when (val result = order.updateInfo(command.pickupTime, command.customerName)) {
        is TakeawayOrder.UpdateResult.Failure.InvalidDate -> InvalidDate
        is TakeawayOrder.UpdateResult.Success -> {
          repository.update(result.order)
          Success
        }
      }
    }
  }
}
