package com.munchies.order.domain.model

/**
 * Domain events of the event-sourced [Order] aggregate.
 *
 * The ordered stream of these events is the single source of truth for an order: the current
 * state is obtained by replaying them through [Order.apply].
 *
 * @property orderId The identifier of the order (aggregate) the event belongs to.
 * @property occurredAt The instant the event happened, in milliseconds since epoch.
 */
sealed interface OrderEvent {
  val orderId: OrderId
  val occurredAt: Long
}

/**
 * The order has been placed. It is always the first event of an order stream and carries the
 * full initial state.
 *
 * @property details The type-specific details.
 */
data class OrderPlaced(
  override val orderId: OrderId,
  val restaurantId: RestaurantId,
  val customerId: CustomerId,
  val items: List<OrderItem>,
  val details: OrderDetails,
  override val occurredAt: Long = System.currentTimeMillis(),
) : OrderEvent

/**
 * The order moved forward in its workflow, from [previousStatus] to [newStatus].
 */
data class OrderStatusAdvanced(
  override val orderId: OrderId,
  val previousStatus: OrderStatus,
  val newStatus: OrderStatus,
  override val occurredAt: Long = System.currentTimeMillis(),
) : OrderEvent

/**
 * The order has been cancelled by the customer.
 */
data class OrderCancelled(
  override val orderId: OrderId,
  override val occurredAt: Long = System.currentTimeMillis(),
) : OrderEvent

/**
 * The order has been paid.
 */
data class OrderPaid(
  override val orderId: OrderId,
  override val occurredAt: Long = System.currentTimeMillis(),
) : OrderEvent

/**
 * The items of the order have been replaced with [items].
 */
data class OrderItemsUpdated(
  override val orderId: OrderId,
  val items: List<OrderItem>,
  override val occurredAt: Long = System.currentTimeMillis(),
) : OrderEvent

/**
 * The delivery information of a delivery order has been replaced with [deliveryInfo].
 */
data class DeliveryInfoUpdated(
  override val orderId: OrderId,
  val deliveryInfo: DeliveryInfo,
  override val occurredAt: Long = System.currentTimeMillis(),
) : OrderEvent

/**
 * The pickup information of a takeaway order has been replaced with [takeawayInfo].
 */
data class TakeawayInfoUpdated(
  override val orderId: OrderId,
  val takeawayInfo: TakeawayInfo,
  override val occurredAt: Long = System.currentTimeMillis(),
) : OrderEvent
