package com.munchies.order.application.usecase

import com.munchies.order.application.eventsourcing.loadOrder
import com.munchies.order.application.port.inbound.UpdateTakeawayOrderInfo
import com.munchies.order.application.port.inbound.UpdateTakeawayOrderInfo.Result.Failure.*
import com.munchies.order.application.port.inbound.UpdateTakeawayOrderInfo.Result.Success
import com.munchies.order.application.port.inbound.command.UpdateTakeawayOrderCommand
import com.munchies.order.domain.model.TakeawayOrder
import com.munchies.order.domain.port.OrderEventStore

/**
 * Use case implementation for updating the information of a takeaway order.
 *
 * This class handles the business logic for updating the pickup time and customer name
 * of a takeaway order, by appending a [com.munchies.order.domain.model.TakeawayInfoUpdated]
 * event to the order stream.
 *
 * @property eventStore The event store holding the order streams.
 */
class UpdateTakeawayOrderInfoUseCase(
  private val eventStore: OrderEventStore,
) : UpdateTakeawayOrderInfo {

  override fun execute(command: UpdateTakeawayOrderCommand): UpdateTakeawayOrderInfo.Result {
    val (order, version) = eventStore.loadOrder(command.orderId) ?: return OrderNotFound

    return when {
      order.customerId != command.customerId -> Unauthorized
      order !is TakeawayOrder -> OrderNotFound
      else -> when (val result = order.updateInfo(command.pickupTime, command.customerName)) {
        is TakeawayOrder.UpdateResult.Failure.InvalidDate -> InvalidDate
        is TakeawayOrder.UpdateResult.Success -> {
          eventStore.append(order.id, version, result.events)
          Success
        }
      }
    }
  }
}
