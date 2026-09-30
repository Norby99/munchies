package com.munchies.order.application.usecase

import com.munchies.order.application.eventsourcing.loadOrder
import com.munchies.order.application.port.inbound.DiscardOrder
import com.munchies.order.application.port.inbound.DiscardOrder.Result.*
import com.munchies.order.application.port.inbound.DiscardOrder.Result.Failure.*
import com.munchies.order.application.port.inbound.command.DiscardOrderCommand
import com.munchies.order.domain.model.Order
import com.munchies.order.domain.port.OrderEventStore

/**
 * Use case implementation for discarding an order.
 *
 * The order can only be discarded if it is in a cancellable state. An
 * [com.munchies.order.domain.model.event.OrderCancelled] event is appended and the order ends up in
 * the CANCELLED status, keeping its full history.
 *
 * @property eventStore The event store holding the order streams.
 */
class DiscardOrderUseCase(private val eventStore: OrderEventStore) : DiscardOrder {
  override fun execute(command: DiscardOrderCommand): DiscardOrder.Result {
    val (order, version) = eventStore.loadOrder(command.orderId) ?: return OrderNotFound

    return when (val result = order.cancel()) {
      is Order.CancelResult.Failure.InvalidTransition -> OrderNotCancellable
      is Order.CancelResult.Success -> {
        eventStore.append(order.id, version, result.events)
        Success
      }
    }
  }
}
