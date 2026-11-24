package com.munchies.order.domain.port

import com.munchies.order.domain.model.Order

/**
 * Outbound port used to publish order lifecycle events for downstream consumers.
 * notification-service subscribes to these events to turn them into user- and
 * restaurant-facing notifications.
 */
interface OrderNotificationPublisher {

  /**
   * Publishes the fact that an order has changed in a way its customer or its restaurant can see:
   * it was placed, paid, advanced to its next status or cancelled. The event carries the order's
   * current status, so consumers (the live updates of the frontend) only need it as a signal that
   * this order changed.
   *
   * @param order The order in its new, already persisted state.
   */
  fun publishStatusChanged(order: Order)
}
