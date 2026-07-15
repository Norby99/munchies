package com.munchies.order.application.usecase

import com.munchies.order.application.port.inbound.UpdateTakeawayOrderInfo
import com.munchies.order.application.port.inbound.UpdateTakeawayOrderInfo.Result.Failure.*
import com.munchies.order.application.port.inbound.UpdateTakeawayOrderInfo.Result.Success
import com.munchies.order.application.port.inbound.command.UpdateTakeawayOrderCommand
import com.munchies.order.domain.model.TakeawayOrder
import com.munchies.order.domain.ports.OrderRepository

/**
 * Use case implementation for updating the information of a takeaway order.
 *
 * This class handles the business logic for updating the pickup time and customer name
 * of a takeaway order. It interacts with the OrderRepository to retrieve and update order data.
 *
 * @property repository The repository used to access and modify order data.
 */
class UpdateTakeawayOrderInfoUseCase(
  private val repository: OrderRepository,
) : UpdateTakeawayOrderInfo {

  override fun execute(command: UpdateTakeawayOrderCommand): UpdateTakeawayOrderInfo.Result {
    val order = repository.findById(command.orderId)

    return when {
      order == null -> OrderNotFound
      order.customerId != command.customerId -> Unauthorized
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
