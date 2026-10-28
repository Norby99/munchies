package com.munchies.order.domain.model.view

import com.munchies.order.domain.model.OrderStatus
import com.munchies.order.domain.model.event.DeliveryInfoUpdated
import com.munchies.order.domain.model.event.OrderCancelled
import com.munchies.order.domain.model.event.OrderItemsUpdated
import com.munchies.order.domain.model.event.OrderPaid
import com.munchies.order.domain.model.event.OrderPlaced
import com.munchies.order.fixtures.Address2
import com.munchies.order.fixtures.asHistory
import com.munchies.order.fixtures.createDeliveryInfo
import com.munchies.order.fixtures.createDeliveryOrder
import com.munchies.order.fixtures.createTakeawayOrder
import com.munchies.order.fixtures.defaultOrderId
import com.munchies.order.fixtures.orderItem2
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class OrderViewUnitTest {

  @Test
  fun `from should project a freshly placed order at version 1`() {
    val placed = createDeliveryOrder().asHistory().single() as OrderPlaced

    val view = OrderView.from(placed)

    view.orderId shouldBe placed.orderId
    view.status shouldBe OrderStatus.PENDING
    view.payed shouldBe false
    view.details shouldBe placed.details
    view.placedAt shouldBe placed.occurredAt
    view.version shouldBe 1
  }

  @Test
  fun `fromHistory should mirror the state of the replayed aggregate`() {
    val order = createDeliveryOrder(status = OrderStatus.READY).copy(payed = true)
    val history = order.asHistory() + DeliveryInfoUpdated(
      defaultOrderId,
      createDeliveryInfo(address = Address2),
    ) + OrderItemsUpdated(defaultOrderId, listOf(orderItem2))

    val view = OrderView.fromHistory(history).shouldNotBeNull()

    view.status shouldBe OrderStatus.READY
    view.payed shouldBe true
    view.details shouldBe createDeliveryInfo(address = Address2)
    view.items shouldBe listOf(orderItem2)
    view.version shouldBe history.size.toLong()
    view.lastUpdatedAt shouldBe history.last().occurredAt
  }

  @Test
  fun `apply should mark cancelled orders`() {
    val placed = createTakeawayOrder().asHistory().single() as OrderPlaced

    val view = OrderView.from(placed).apply(OrderCancelled(defaultOrderId))

    view.status shouldBe OrderStatus.CANCELLED
    view.version shouldBe 2
  }

  @Test
  fun `apply should ignore info updates of another order type but still bump the version`() {
    val placed = createTakeawayOrder().asHistory().single() as OrderPlaced
    val view = OrderView.from(placed)

    val updated = view.apply(DeliveryInfoUpdated(defaultOrderId, createDeliveryInfo()))

    updated.details shouldBe view.details
    updated.version shouldBe 2
  }

  @Test
  fun `fromHistory should return null when the stream does not start with OrderPlaced`() {
    OrderView.fromHistory(listOf(OrderPaid(defaultOrderId))).shouldBeNull()
  }
}
