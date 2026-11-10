package com.munchies.order.infrastructure.adapter.inbound.web.config

import com.munchies.order.domain.port.MenuCatalog
import com.munchies.order.infrastructure.adapter.outbound.restaurant.RestaurantMenuCatalog
import com.munchies.order.infrastructure.adapter.outbound.restaurant.RestaurantServiceClient
import io.micronaut.context.annotation.Factory
import jakarta.inject.Singleton

/**
 * Wiring of the outbound adapters towards other microservices.
 */
@Factory
class OrderOutboundBeans {

  /** Item prices come from restaurant-service (see [RestaurantMenuCatalog]). */
  @Singleton
  fun menuCatalog(client: RestaurantServiceClient): MenuCatalog = RestaurantMenuCatalog(client)
}
