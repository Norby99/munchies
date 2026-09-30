package com.munchies.order.application.eventsourcing

import com.munchies.order.domain.factory.OrderFactory
import com.munchies.order.domain.model.Order
import com.munchies.order.domain.model.OrderId
import com.munchies.order.domain.port.OrderEventStore

/**
 * Event sourcing support shared by the command use cases.
 *
 * The current state of an event-sourced order, together with the version of the stream
 * it was rebuilt from. The version is the `expectedVersion` to pass to
 * [OrderEventStore.append] when appending the events decided against this state.
 */
internal data class EventSourcedOrder(val order: Order, val version: Long)

/**
 * Loads the event stream of [orderId] and replays it into the current state of the order.
 *
 * @return The rebuilt order and its stream version, or `null` if the order does not exist.
 */
internal fun OrderEventStore.loadOrder(orderId: OrderId): EventSourcedOrder? {
  val history = load(orderId)
  val order = OrderFactory.fromHistory(history) ?: return null
  return EventSourcedOrder(order, history.size.toLong())
}
