package com.munchies.order.fixtures

import com.munchies.order.domain.model.DeliveryOrder
import com.munchies.order.domain.model.DineInOrder
import com.munchies.order.domain.model.Order
import com.munchies.order.domain.model.OrderStatus
import com.munchies.order.domain.model.TakeawayOrder
import com.munchies.order.domain.model.event.OrderCancelled
import com.munchies.order.domain.model.event.OrderEvent
import com.munchies.order.domain.model.event.OrderPaid
import com.munchies.order.domain.model.event.OrderPlaced
import com.munchies.order.domain.model.view.OrderView

private const val MAX_STATUS_STEPS = 10

/**
 * Builds a valid event stream whose replay yields exactly this order: an [OrderPlaced], the
 * status transitions needed to reach [Order.status] (or an [OrderCancelled]) and, if the order
 * is paid, an [OrderPaid]. Lets tests express "the event store contains this order" in terms
 * of the familiar order fixtures.
 */
fun Order.asHistory(): List<OrderEvent> {
  val placed = OrderPlaced(
    orderId = id,
    restaurantId = restaurantId,
    customerId = customerId,
    items = items,
    details = when (this) {
      is DeliveryOrder -> deliveryInfo
      is TakeawayOrder -> takeawayInfo
      is DineInOrder -> tableInfo
    },
  )
  val history = mutableListOf<OrderEvent>(placed)
  var current = Order.from(placed)

  if (status == OrderStatus.CANCELLED) {
    history += OrderCancelled(id)
  } else {
    repeat(MAX_STATUS_STEPS) {
      if (current.status == status) return@repeat
      val next = current.nextStatus() as Order.AdvanceStatusResult.Success
      history += next.events
      current = current.applyAll(next.events)
    }
    check(current.status == status) { "Status $status is not reachable for $this" }
  }
  if (payed) history += OrderPaid(id)
  return history
}

/**
 * Applies [events] to this order, keeping its static subtype (apply never changes the subtype).
 */
@Suppress("UNCHECKED_CAST")
fun <T : Order> T.after(events: List<OrderEvent>): T = applyAll(events) as T

/**
 * Builds the read model view that the projection would produce for this order.
 */
fun Order.asView(): OrderView = checkNotNull(OrderView.fromHistory(asHistory()))
