package com.munchies.order.domain.port

import com.munchies.order.domain.model.OrderEvent
import com.munchies.order.domain.model.OrderId

/**
 * Outbound port of the command side: the append-only event store of the [OrderEvent] streams.
 *
 * It is the single source of truth for orders and replaces the former CRUD repository: an order
 * is never saved or updated as a whole, its new events are appended to its stream instead.
 * Each stream is addressed by order id and its events are numbered by a zero-based, strictly
 * increasing sequence.
 */
interface OrderEventStore {

  /**
   * Loads the full, ordered event stream of an order.
   *
   * @param orderId The order whose stream is loaded.
   * @return The events in sequence order, or an empty list if the order does not exist.
   */
  fun load(orderId: OrderId): List<OrderEvent>

  /**
   * Appends [events] to the stream of [orderId], starting at sequence [expectedVersion].
   *
   * [expectedVersion] is the number of events the caller replayed before deciding on the new
   * ones (0 for a new order). If another writer appended to the same stream in the meantime, the
   * append is rejected as a whole: this is the optimistic concurrency control of the aggregate.
   *
   * @throws ConcurrentOrderModificationException if the stream no longer has [expectedVersion]
   * events.
   */
  fun append(orderId: OrderId, expectedVersion: Long, events: List<OrderEvent>)
}

/**
 * Thrown by [OrderEventStore.append] when the order stream was modified concurrently, i.e. the
 * events being appended were decided against a stale state of the aggregate.
 */
class ConcurrentOrderModificationException(orderId: OrderId) :
  RuntimeException("Order ${orderId.value} was modified concurrently")
