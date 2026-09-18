package com.munchies.order.domain.port

import com.munchies.order.domain.model.Order

/**
 * Outbound port used to publish order lifecycle events for downstream consumers.
 * notification-service subscribes to these events to turn them into user- and
 * restaurant-facing notifications.
 */
interface OrderNotificationPublisher {

  /**
   * Publishes the fact that an order's status has changed.
   *
   * @param order The order in its new, already persisted state.
   */
  fun publishStatusChanged(order: Order)
}
