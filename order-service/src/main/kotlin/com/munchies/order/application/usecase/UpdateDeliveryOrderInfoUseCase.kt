package com.munchies.order.application.usecase

import com.munchies.order.application.eventsourcing.loadOrder
import com.munchies.order.application.port.inbound.UpdateDeliveryOrderInfo
import com.munchies.order.application.port.inbound.UpdateDeliveryOrderInfo.Result.Failure.*
import com.munchies.order.application.port.inbound.UpdateDeliveryOrderInfo.Result.Success
import com.munchies.order.application.port.inbound.command.UpdateDeliveryOrderCommand
import com.munchies.order.domain.model.DeliveryOrder
import com.munchies.order.domain.port.OrderEventStore

/**
 * Use case implementation for updating the information of a delivery order.
 *
 * This class handles the business logic for updating delivery order details such as estimated
 * delivery time, delivery address, bell name, and customer phone number, by appending a
 * [com.munchies.order.domain.model.event.DeliveryInfoUpdated] event to the order stream.
 *
 * @property eventStore The event store holding the order streams.
 */
class UpdateDeliveryOrderInfoUseCase(private val eventStore: OrderEventStore) :
  UpdateDeliveryOrderInfo {
  override fun execute(command: UpdateDeliveryOrderCommand): UpdateDeliveryOrderInfo.Result {
    val (order, version) = eventStore.loadOrder(command.orderId) ?: return OrderNotFound

    return when {
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
          eventStore.append(order.id, version, result.events)
          Success
        }
      }
    }
  }
}
