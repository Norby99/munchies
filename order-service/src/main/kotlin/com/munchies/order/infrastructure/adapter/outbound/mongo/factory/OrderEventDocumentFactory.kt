package com.munchies.order.infrastructure.adapter.outbound.mongo.factory

import com.munchies.order.domain.model.CustomerId
import com.munchies.order.domain.model.OrderId
import com.munchies.order.domain.model.OrderStatus
import com.munchies.order.domain.model.RestaurantId
import com.munchies.order.domain.model.event.DeliveryInfoUpdated
import com.munchies.order.domain.model.event.OrderCancelled
import com.munchies.order.domain.model.event.OrderEvent
import com.munchies.order.domain.model.event.OrderItemsUpdated
import com.munchies.order.domain.model.event.OrderPaid
import com.munchies.order.domain.model.event.OrderPlaced
import com.munchies.order.domain.model.event.OrderStatusAdvanced
import com.munchies.order.domain.model.event.TakeawayInfoUpdated
import com.munchies.order.infrastructure.adapter.outbound.mongo.document.OrderEventDocument
import com.munchies.order.infrastructure.adapter.outbound.mongo.factory.OrderSubDocumentFactory.readItems
import com.munchies.order.infrastructure.adapter.outbound.mongo.factory.OrderSubDocumentFactory.toDeliveryInfo
import com.munchies.order.infrastructure.adapter.outbound.mongo.factory.OrderSubDocumentFactory.toDetailsDocument
import com.munchies.order.infrastructure.adapter.outbound.mongo.factory.OrderSubDocumentFactory.toDocument
import com.munchies.order.infrastructure.adapter.outbound.mongo.factory.OrderSubDocumentFactory.toItemsDocument
import com.munchies.order.infrastructure.adapter.outbound.mongo.factory.OrderSubDocumentFactory.toOrderDetails
import com.munchies.order.infrastructure.adapter.outbound.mongo.factory.OrderSubDocumentFactory.toTakeawayInfo
import java.util.Date
import org.bson.Document

/**
 * Mapping between [OrderEvent]s and the documents of the event store collection
 * (see [OrderEventDocument] for the schema).
 *
 * Events are stored forever, so the payload schema of each event type is versioned through
 * `eventVersion`. When an event's shape changes, bump [CURRENT_EVENT_VERSION] (or a per-type
 * version) and upcast old payloads to the current shape in [toEvent], before building the
 * domain event: stored payloads are never rewritten.
 */
object OrderEventDocumentFactory {

  const val CURRENT_EVENT_VERSION = 1

  private const val RESTAURANT_ID = "restaurantId"
  private const val CUSTOMER_ID = "customerId"
  private const val ITEMS = "items"
  private const val DETAILS = "details"
  private const val PREVIOUS_STATUS = "previousStatus"
  private const val NEW_STATUS = "newStatus"
  private const val DELIVERY_INFO = "deliveryInfo"
  private const val TAKEAWAY_INFO = "takeawayInfo"

  /**
   * Builds the store document of [event], placed at [sequence] of its order stream.
   */
  fun toDocument(event: OrderEvent, sequence: Long): Document =
    Document(OrderEventDocument.AGGREGATE_TYPE, OrderEventDocument.ORDER_AGGREGATE_TYPE)
      .append(OrderEventDocument.AGGREGATE_ID, event.orderId.value)
      .append(OrderEventDocument.SEQUENCE, sequence)
      .append(OrderEventDocument.EVENT_TYPE, event.typeName())
      .append(OrderEventDocument.EVENT_VERSION, CURRENT_EVENT_VERSION)
      .append(OrderEventDocument.PAYLOAD, event.payload())
      .append(OrderEventDocument.OCCURRED_AT, Date(event.occurredAt))

  /**
   * Rebuilds the domain event stored in [document].
   *
   * @throws IllegalStateException if the event type is unknown.
   */
  fun toEvent(document: Document): OrderEvent {
    val orderId = OrderId(document.getString(OrderEventDocument.AGGREGATE_ID))
    val occurredAt = document.getDate(OrderEventDocument.OCCURRED_AT).time
    // Only version 1 exists so far: this is where older payloads would be upcast.
    val payload = document.get(OrderEventDocument.PAYLOAD, Document::class.java)

    return when (val type = document.getString(OrderEventDocument.EVENT_TYPE)) {
      ORDER_PLACED -> OrderPlaced(
        orderId = orderId,
        restaurantId = RestaurantId(payload.getString(RESTAURANT_ID)),
        customerId = CustomerId(payload.getString(CUSTOMER_ID)),
        items = payload.readItems(ITEMS),
        details = payload.get(DETAILS, Document::class.java).toOrderDetails(),
        occurredAt = occurredAt,
      )
      ORDER_STATUS_ADVANCED -> OrderStatusAdvanced(
        orderId = orderId,
        previousStatus = OrderStatus.valueOf(payload.getString(PREVIOUS_STATUS)),
        newStatus = OrderStatus.valueOf(payload.getString(NEW_STATUS)),
        occurredAt = occurredAt,
      )
      ORDER_CANCELLED -> OrderCancelled(orderId, occurredAt)
      ORDER_PAID -> OrderPaid(orderId, occurredAt)
      ORDER_ITEMS_UPDATED -> OrderItemsUpdated(orderId, payload.readItems(ITEMS), occurredAt)
      DELIVERY_INFO_UPDATED -> DeliveryInfoUpdated(
        orderId,
        payload.get(DELIVERY_INFO, Document::class.java).toDeliveryInfo(),
        occurredAt,
      )
      TAKEAWAY_INFO_UPDATED -> TakeawayInfoUpdated(
        orderId,
        payload.get(TAKEAWAY_INFO, Document::class.java).toTakeawayInfo(),
        occurredAt,
      )
      else -> error("Unknown order event type: $type")
    }
  }

  private fun OrderEvent.typeName(): String = when (this) {
    is OrderPlaced -> ORDER_PLACED
    is OrderStatusAdvanced -> ORDER_STATUS_ADVANCED
    is OrderCancelled -> ORDER_CANCELLED
    is OrderPaid -> ORDER_PAID
    is OrderItemsUpdated -> ORDER_ITEMS_UPDATED
    is DeliveryInfoUpdated -> DELIVERY_INFO_UPDATED
    is TakeawayInfoUpdated -> TAKEAWAY_INFO_UPDATED
  }

  private fun OrderEvent.payload(): Document = when (this) {
    is OrderPlaced -> Document(RESTAURANT_ID, restaurantId.value)
      .append(CUSTOMER_ID, customerId.value)
      .append(ITEMS, items.toItemsDocument())
      .append(DETAILS, details.toDetailsDocument())
    is OrderStatusAdvanced -> Document(PREVIOUS_STATUS, previousStatus.name)
      .append(NEW_STATUS, newStatus.name)
    is OrderCancelled, is OrderPaid -> Document()
    is OrderItemsUpdated -> Document(ITEMS, items.toItemsDocument())
    is DeliveryInfoUpdated -> Document(DELIVERY_INFO, deliveryInfo.toDocument())
    is TakeawayInfoUpdated -> Document(TAKEAWAY_INFO, takeawayInfo.toDocument())
  }

  // Stored event type names: part of the persisted schema, never rename them.
  private const val ORDER_PLACED = "OrderPlaced"
  private const val ORDER_STATUS_ADVANCED = "OrderStatusAdvanced"
  private const val ORDER_CANCELLED = "OrderCancelled"
  private const val ORDER_PAID = "OrderPaid"
  private const val ORDER_ITEMS_UPDATED = "OrderItemsUpdated"
  private const val DELIVERY_INFO_UPDATED = "DeliveryInfoUpdated"
  private const val TAKEAWAY_INFO_UPDATED = "TakeawayInfoUpdated"
}
