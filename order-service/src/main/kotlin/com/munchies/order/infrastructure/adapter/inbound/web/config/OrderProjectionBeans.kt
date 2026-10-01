package com.munchies.order.infrastructure.adapter.inbound.web.config

import com.munchies.order.application.projection.OrderViewProjector
import com.munchies.order.domain.port.OrderEventStore
import com.munchies.order.domain.port.OrderViewRepository
import io.micronaut.context.annotation.Factory
import jakarta.inject.Singleton

/**
 * Wiring of the CQRS projector: the component that reads the event store (command side) and
 * writes the read model (query side). It is not exposed through [OrderServices], because it
 * cannot be called from outside.
 */
@Factory
class OrderProjectionBeans {

  @Singleton
  fun orderViewProjector(
    views: OrderViewRepository,
    eventStore: OrderEventStore,
  ): OrderViewProjector = OrderViewProjector(views, eventStore)
}
