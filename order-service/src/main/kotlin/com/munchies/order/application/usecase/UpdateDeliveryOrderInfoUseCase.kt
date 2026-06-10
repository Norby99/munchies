package com.munchies.order.application.usecase

import com.munchies.order.application.port.inbound.UpdateDeliveryOrderInfo
import com.munchies.order.application.port.inbound.UpdateDeliveryOrderInfo.Result.Failure.*
import com.munchies.order.application.port.inbound.UpdateDeliveryOrderInfo.Result.Success
import com.munchies.order.application.port.inbound.command.UpdateDeliveryOrderCommand
import com.munchies.order.domain.model.DeliveryInfo
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
      command.estimatedDeliveryTime <= System.currentTimeMillis() -> InvalidDate
      else -> {
        val updatedOrder = (order as DeliveryOrder).copy(
          deliveryInfo = DeliveryInfo(
            estimatedDeliveryTime = command.estimatedDeliveryTime,
            deliveryAddress = command.deliveryAddress,
            bellName = command.bellName,
            customerPhone = command.customerPhone,
          ),
        )
        repository.update(updatedOrder)
        Success
      }
    }
  }
}
