package com.munchies.order.infrastructure.adapter.outbound.mongo.factory

import com.munchies.order.domain.model.OrderStatus
import com.munchies.order.fixtures.asView
import com.munchies.order.fixtures.createDeliveryOrder
import com.munchies.order.fixtures.createDineInOrder
import com.munchies.order.fixtures.createTakeawayOrder
import com.munchies.order.infrastructure.adapter.outbound.mongo.document.OrderViewDocument
import com.munchies.order.infrastructure.adapter.outbound.mongo.factory.OrderViewDocumentFactory.toDocument
import com.munchies.order.infrastructure.adapter.outbound.mongo.factory.OrderViewDocumentFactory.toNullableView
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class OrderViewDocumentFactoryUnitTest {

  @Test
  fun `toNullableView should rebuild exactly the view written by toDocument`() {
    listOf(
      createDeliveryOrder(status = OrderStatus.ON_THE_WAY).copy(payed = true).asView(),
      createTakeawayOrder(status = OrderStatus.CANCELLED).asView(),
      createDineInOrder(status = OrderStatus.READY).asView(),
    ).forEach { view ->
      view.toDocument().toNullableView() shouldBe view
    }
  }

  @Test
  fun `toDocument should use the order id as document id`() {
    val view = createDeliveryOrder().asView()

    view.toDocument().getString(OrderViewDocument.ID) shouldBe view.orderId.value
  }

  @Test
  fun `toNullableView should return null for a malformed document`() {
    val document = createDeliveryOrder().asView().toDocument()
      .append(OrderViewDocument.STATUS, "TELEPORTED")

    document.toNullableView().shouldBeNull()
  }
}
