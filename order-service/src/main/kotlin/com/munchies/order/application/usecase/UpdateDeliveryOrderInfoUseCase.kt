package com.munchies.order.application.usecase

import com.munchies.order.application.port.inbound.UpdateDeliveryOrderInfo
import com.munchies.order.application.port.inbound.UpdateDeliveryOrderInfo.Result.Failure.*
import com.munchies.order.application.port.inbound.UpdateDeliveryOrderInfo.Result.Success
import com.munchies.order.application.port.inbound.command.UpdateDeliveryOrderCommand
import com.munchies.order.domain.model.DeliveryOrder
import com.munchies.order.domain.model.OrderId
import com.munchies.order.domain.ports.OrderRepository

class UpdateDeliveryOrderInfoUseCase(private val repository: OrderRepository) :
  UpdateDeliveryOrderInfo {
  override fun execute(command: UpdateDeliveryOrderCommand): UpdateDeliveryOrderInfo.Result {
    val order = repository.findById(OrderId(command.orderId))

    return when {
      order == null -> OrderNotFound
      order.customerId.value != command.customerId -> Unauthorized
      order !is DeliveryOrder -> OrderNotFound
      else -> when (
        val result = order.updateInfo(
          command.estimatedDeliveryTime,
          command.deliveryAddress,
          command.bellName,
          command.customerPhone,
        )
      ) {
        is DeliveryOrder.UpdateResult.Failure.InvalidDate -> InvalidDate
        is DeliveryOrder.UpdateResult.Success -> {
          repository.update(result.order)
          Success
        }
      }
    }
  }
}
