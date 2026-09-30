package com.munchies.order.application.usecase

import com.munchies.order.application.eventsourcing.loadOrder
import com.munchies.order.application.port.inbound.PayOrder
import com.munchies.order.application.port.inbound.command.PayOrderCommand
import com.munchies.order.domain.model.Order
import com.munchies.order.domain.port.OrderEventStore

/**
 * Use case implementation for flagging an order as paid, by appending an
 * [com.munchies.order.domain.model.OrderPaid] event to its stream.
 *
 * @property eventStore The event store holding the order streams.
 */
class PayOrderUseCase(private val eventStore: OrderEventStore) : PayOrder {
  override fun execute(command: PayOrderCommand): PayOrder.Result {
    val (order, version) = eventStore.loadOrder(command.orderId)
      ?: return PayOrder.Result.Failure.OrderNotFound

    return when (val result = order.pay()) {
      is Order.PayResult.Failure.AlreadyPaid ->
        PayOrder.Result.Failure.AlreadyPaid
      is Order.PayResult.Success -> {
        eventStore.append(order.id, version, result.events)
        PayOrder.Result.Success
      }
    }
  }
}
