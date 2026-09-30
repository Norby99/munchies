package com.munchies.order.domain.port

import com.munchies.order.domain.model.CustomerId
import com.munchies.order.domain.model.OrderId
import com.munchies.order.domain.model.OrderStatus
import com.munchies.order.domain.model.OrderView
import com.munchies.order.domain.model.RestaurantId

/**
 * Outbound port of the query side (CQRS): storage of the [OrderView] projection.
 *
 * Query use cases depend only on this port, never on [OrderEventStore]. The only writer of the
 * projection is the projection use case that folds newly appended events into it; no other
 * code is allowed to mutate a view.
 */
interface OrderViewRepository {

  /**
   * @return The view of the order with id [orderId], or `null` if it is not (yet) projected.
   */
  fun findById(orderId: OrderId): OrderView?

  /**
   * Finds the views matching all the given filters; a `null` filter is ignored.
   *
   * @return The matching views, possibly empty.
   */
  fun findBy(
    restaurantId: RestaurantId?,
    customerId: CustomerId?,
    status: OrderStatus?,
  ): List<OrderView>

  /**
   * Inserts or replaces the view of [view]'s order.
   */
  fun save(view: OrderView)
}
