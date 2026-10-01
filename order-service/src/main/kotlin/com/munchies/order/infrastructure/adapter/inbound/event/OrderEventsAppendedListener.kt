package com.munchies.order.infrastructure.adapter.inbound.event

import com.munchies.order.application.projection.OrderViewProjector
import io.micronaut.runtime.event.annotation.EventListener
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

/**
 * Inbound adapter subscribing to [OrderEventsAppended] and delegating to the
 * [OrderViewProjector], which updates the CQRS read model.
 *
 * The listener runs synchronously, inside the request that appended the events, so the read
 * model is normally up to date as soon as the command returns. A projection failure, however,
 * must never fail the command: the events are already durable in the event store, which is the
 * source of truth. The failure is logged and the view catches up on the next append for the same
 * order, when the projector detects the sequence gap and rebuilds the view from the stream.
 */
@Singleton
class OrderEventsAppendedListener(
  private val projector: OrderViewProjector,
) {

  @EventListener
  fun onEventsAppended(appended: OrderEventsAppended) {
    runCatching {
      projector.project(appended.orderId, appended.firstSequence, appended.events)
    }.onFailure {
      logger.error(
        "Failed to project events {}..{} of order {}",
        appended.firstSequence,
        appended.firstSequence + appended.events.size - 1,
        appended.orderId.value,
        it,
      )
    }
  }

  private companion object {
    val logger = LoggerFactory.getLogger(OrderEventsAppendedListener::class.java)
  }
}
