package com.munchies.order.infrastructure.adapter.outbound.mongo.factory

import com.munchies.order.domain.model.OrderStatus
import com.munchies.order.domain.model.event.DeliveryInfoUpdated
import com.munchies.order.domain.model.event.OrderCancelled
import com.munchies.order.domain.model.event.OrderEvent
import com.munchies.order.domain.model.event.OrderItemsUpdated
import com.munchies.order.domain.model.event.OrderPaid
import com.munchies.order.domain.model.event.OrderPlaced
import com.munchies.order.domain.model.event.OrderStatusAdvanced
import com.munchies.order.domain.model.event.TakeawayInfoUpdated
import com.munchies.order.fixtures.createDeliveryInfo
import com.munchies.order.fixtures.createNewItems
import com.munchies.order.fixtures.createTakeawayInfo
import com.munchies.order.fixtures.defaultCustomerId
import com.munchies.order.fixtures.defaultOrderId
import com.munchies.order.fixtures.defaultRestaurantId
import com.munchies.order.fixtures.defaultTableInfo
import com.munchies.order.infrastructure.adapter.outbound.mongo.document.OrderEventDocument
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class OrderEventDocumentFactoryUnitTest {

  companion object {
    // Millisecond precision, as stored by MongoDB dates.
    private const val OCCURRED_AT = 1_700_000_000_000L

    @JvmStatic
    fun events(): List<OrderEvent> = listOf(
      OrderPlaced(
        defaultOrderId,
        defaultRestaurantId,
        defaultCustomerId,
        createNewItems(),
        createDeliveryInfo(),
        OCCURRED_AT,
      ),
      OrderPlaced(
        defaultOrderId,
        defaultRestaurantId,
        defaultCustomerId,
        createNewItems(),
        createTakeawayInfo(),
        OCCURRED_AT,
      ),
      OrderPlaced(
        defaultOrderId,
        defaultRestaurantId,
        defaultCustomerId,
        createNewItems(),
        defaultTableInfo(),
        OCCURRED_AT,
      ),
      OrderStatusAdvanced(defaultOrderId, OrderStatus.PENDING, OrderStatus.PREPARING, OCCURRED_AT),
      OrderCancelled(defaultOrderId, OCCURRED_AT),
      OrderPaid(defaultOrderId, OCCURRED_AT),
      OrderItemsUpdated(defaultOrderId, createNewItems(), OCCURRED_AT),
      DeliveryInfoUpdated(defaultOrderId, createDeliveryInfo(), OCCURRED_AT),
      TakeawayInfoUpdated(defaultOrderId, createTakeawayInfo(), OCCURRED_AT),
    )
  }

  @ParameterizedTest
  @MethodSource("events")
  fun `toEvent should rebuild exactly the event written by toDocument`(event: OrderEvent) {
    val document = OrderEventDocumentFactory.toDocument(event, sequence = 4)

    OrderEventDocumentFactory.toEvent(document) shouldBe event
  }

  @Test
  fun `toDocument should fill the event store envelope`() {
    val event = OrderPaid(defaultOrderId, OCCURRED_AT)

    val document = OrderEventDocumentFactory.toDocument(event, sequence = 7)

    document.getString(OrderEventDocument.AGGREGATE_TYPE) shouldBe "Order"
    document.getString(OrderEventDocument.AGGREGATE_ID) shouldBe defaultOrderId.value
    document.getLong(OrderEventDocument.SEQUENCE) shouldBe 7L
    document.getString(OrderEventDocument.EVENT_TYPE) shouldBe "OrderPaid"
    document.getInteger(OrderEventDocument.EVENT_VERSION) shouldBe
      OrderEventDocumentFactory.CURRENT_EVENT_VERSION
  }

  @Test
  fun `toEvent should reject unknown event types`() {
    val document = OrderEventDocumentFactory.toDocument(OrderPaid(defaultOrderId), 0)
      .append(OrderEventDocument.EVENT_TYPE, "OrderTeleported")

    assertThrows<IllegalStateException> { OrderEventDocumentFactory.toEvent(document) }
  }
}
