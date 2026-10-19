package com.munchies.order.domain.model.view

import com.munchies.order.domain.model.CustomerId
import com.munchies.order.domain.model.DeliveryInfo
import com.munchies.order.domain.model.Order
import com.munchies.order.domain.model.OrderDetails
import com.munchies.order.domain.model.OrderId
import com.munchies.order.domain.model.OrderItem
import com.munchies.order.domain.model.OrderStatus
import com.munchies.order.domain.model.RestaurantId
import com.munchies.order.domain.model.TakeawayInfo
import com.munchies.order.domain.model.event.DeliveryInfoUpdated
import com.munchies.order.domain.model.event.OrderCancelled
import com.munchies.order.domain.model.event.OrderEvent
import com.munchies.order.domain.model.event.OrderItemsUpdated
import com.munchies.order.domain.model.event.OrderPaid
import com.munchies.order.domain.model.event.OrderPlaced
import com.munchies.order.domain.model.event.OrderStatusAdvanced
import com.munchies.order.domain.model.event.TakeawayInfoUpdated

/**
 * CQRS read model of an order: a denormalized, query-optimized projection of the [OrderEvent]
 * stream, used to answer the queries the event store cannot serve efficiently (e.g. "all the
 * orders of restaurant X with status Y").
 *
 * It is not the [Order] aggregate and is never used to validate commands: it is disposable and
 * can always be rebuilt from scratch by replaying the event store. It is kept up to date by the
 * projection, which folds each newly appended event into it through [apply].
 *
 * @property orderId The unique identifier of the order.
 * @property restaurantId The restaurant the order belongs to.
 * @property customerId The customer who placed the order.
 * @property status The current status of the order.
 * @property items The current items of the order.
 * @property payed Whether the order has been paid.
 * @property details The type-specific details of the order.
 * @property placedAt When the order was placed, in milliseconds since epoch.
 * @property lastUpdatedAt When the last event was applied, in milliseconds since epoch.
 * @property version The number of events of the stream already folded into this view. Because
 * stream sequences are zero-based, it is also the sequence of the next expected event; it lets
 * the projection skip duplicates and detect gaps.
 */
data class OrderView(
  val orderId: OrderId,
  val restaurantId: RestaurantId,
  val customerId: CustomerId,
  val status: OrderStatus,
  val items: List<OrderItem>,
  val payed: Boolean,
  val details: OrderDetails,
  val placedAt: Long,
  val lastUpdatedAt: Long,
  val version: Long,
) {

  /**
   * Folds [event] into the view and returns the updated view, bumping [version] by one. Like
   * the aggregate's apply, it never fails: events that do not concern this order type leave
   * the data unchanged.
   */
  fun apply(event: OrderEvent): OrderView {
    val updated = when (event) {
      is OrderPlaced -> this
      is OrderStatusAdvanced -> copy(status = event.newStatus)
      is OrderCancelled -> copy(status = OrderStatus.CANCELLED)
      is OrderPaid -> copy(payed = true)
      is OrderItemsUpdated -> copy(items = event.items)
      is DeliveryInfoUpdated ->
        if (details is DeliveryInfo) copy(details = event.deliveryInfo) else this
      is TakeawayInfoUpdated ->
        if (details is TakeawayInfo) copy(details = event.takeawayInfo) else this
    }
    return updated.copy(lastUpdatedAt = event.occurredAt, version = version + 1)
  }

  companion object {
    /**
     * Creates the view of a freshly placed order from its creation event (sequence 0).
     */
    fun from(event: OrderPlaced): OrderView = OrderView(
      orderId = event.orderId,
      restaurantId = event.restaurantId,
      customerId = event.customerId,
      status = OrderStatus.PENDING,
      items = event.items,
      payed = false,
      details = event.details,
      placedAt = event.occurredAt,
      lastUpdatedAt = event.occurredAt,
      version = 1,
    )

    /**
     * Rebuilds the view by folding a whole event stream, or returns `null` if the stream does
     * not start with an [OrderPlaced] event.
     */
    fun fromHistory(history: List<OrderEvent>): OrderView? {
      val placed = history.firstOrNull() as? OrderPlaced ?: return null
      return history.drop(1).fold(from(placed)) { view, event -> view.apply(event) }
    }
  }
}
