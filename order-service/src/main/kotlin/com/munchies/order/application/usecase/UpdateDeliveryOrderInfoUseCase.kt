package com.munchies.order.application.usecase

import com.munchies.order.application.port.inbound.UpdateDeliveryOrderInfo
import com.munchies.order.application.port.inbound.UpdateDeliveryOrderInfo.Result.Failure.*
import com.munchies.order.application.port.inbound.UpdateDeliveryOrderInfo.Result.Success
import com.munchies.order.application.port.inbound.command.UpdateDeliveryOrderCommand
import com.munchies.order.domain.model.DeliveryOrder
import com.munchies.order.domain.ports.OrderRepository

/**
 * Use case implementation for updating the information of a delivery order.
 *
 * This class handles the business logic for updating delivery order details such as estimated delivery time,
 * delivery address, bell name, and customer phone number. It interacts with the OrderRepository to retrieve
 * and update order data.
 *
 * @property repository The repository used to access and modify order data.
 */
class UpdateDeliveryOrderInfoUseCase(private val repository: OrderRepository) :
  UpdateDeliveryOrderInfo {
  override fun execute(command: UpdateDeliveryOrderCommand): UpdateDeliveryOrderInfo.Result {
    val order = repository.findById(command.orderId)

    return when {
      order == null -> OrderNotFound
      order.customerId != command.customerId -> Unauthorized
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
