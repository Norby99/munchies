package com.munchies.order.infrastructure.adapter.inbound.event

import com.munchies.order.domain.model.OrderId
import com.munchies.order.domain.model.event.OrderEvent

/**
 * In-process application event published by the event store adapter right after a batch of
 * events has been durably appended to an order stream.
 *
 * It turns the event store into the change feed of the CQRS query side (the "database +
 * message broker" role of an event store), without involving Kafka: the projection lives in
 * this same service, and Kafka is reserved for notification-service.
 *
 * @property orderId The order whose stream was appended to.
 * @property firstSequence The stream sequence of the first event of [events].
 * @property events The appended events, in sequence order.
 */
data class OrderEventsAppended(
  val orderId: OrderId,
  val firstSequence: Long,
  val events: List<OrderEvent>,
)
