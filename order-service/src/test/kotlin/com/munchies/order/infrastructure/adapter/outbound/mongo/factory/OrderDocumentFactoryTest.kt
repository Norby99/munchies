package com.munchies.order.infrastructure.adapter.outbound.mongo.factory

import com.munchies.order.domain.model.DeliveryOrder
import com.munchies.order.domain.model.DineInOrder
import com.munchies.order.domain.model.TakeawayOrder
import com.munchies.order.fixtures.createDeliveryOrder
import com.munchies.order.fixtures.createDeliveryOrderDocument
import com.munchies.order.fixtures.createDineInOrder
import com.munchies.order.fixtures.createDineInOrderDocument
import com.munchies.order.fixtures.createTakeawayOrder
import com.munchies.order.fixtures.createTakeawayOrderDocument
import com.munchies.order.infrastructure.adapter.dto.OrderType
import com.munchies.order.infrastructure.adapter.outbound.mongo.document.DeliveryInfoDocument
import com.munchies.order.infrastructure.adapter.outbound.mongo.document.TableInfoDocument
import com.munchies.order.infrastructure.adapter.outbound.mongo.document.TakeawayInfoDocument
import com.munchies.order.infrastructure.adapter.outbound.mongo.factory.OrderDocumentFactory.toDocument
import com.munchies.order.infrastructure.adapter.outbound.mongo.factory.OrderDocumentFactory.toNullableDomain
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class OrderDocumentFactoryTest {

  @Test
  fun `toDocument should map DeliveryOrder to OrderDocument with DELIVERY type`() {
    val order = createDeliveryOrder()

    val document = order.toDocument()

    document.id shouldBe order.id.value
    document.orderType shouldBe OrderType.DELIVERY.name
    document.restaurantId shouldBe order.restaurantId.value
    document.customerId shouldBe order.customerId.value
    document.status shouldBe order.status.name
    document.items.size shouldBe order.items.size
    document.deliveryInfo shouldBe DeliveryInfoDocument(
      estimatedDeliveryTime = order.deliveryInfo.estimatedDeliveryTime,
      deliveryAddress = order.deliveryInfo.deliveryAddress,
      bellName = order.deliveryInfo.bellName,
      customerPhone = order.deliveryInfo.customerPhone,
    )
    document.tableInfo.shouldBeNull()
    document.takeawayInfo.shouldBeNull()
  }

  @Test
  fun `toDocument should map DineInOrder to OrderDocument with DINE_IN type`() {
    val order = createDineInOrder()

    val document = order.toDocument()

    document.id shouldBe order.id.value
    document.orderType shouldBe OrderType.DINE_IN.name
    document.restaurantId shouldBe order.restaurantId.value
    document.customerId shouldBe order.customerId.value
    document.status shouldBe order.status.name
    document.items.size shouldBe order.items.size
    document.tableInfo shouldBe TableInfoDocument(
      tableNumber = order.tableInfo.tableNumber,
      numberOfGuests = order.tableInfo.numberOfGuests,
    )
    document.deliveryInfo.shouldBeNull()
    document.takeawayInfo.shouldBeNull()
  }

  @Test
  fun `toDocument should map TakeawayOrder to OrderDocument with TAKEAWAY type`() {
    val order = createTakeawayOrder()

    val document = order.toDocument()

    document.id shouldBe order.id.value
    document.orderType shouldBe OrderType.TAKEAWAY.name
    document.restaurantId shouldBe order.restaurantId.value
    document.customerId shouldBe order.customerId.value
    document.status shouldBe order.status.name
    document.items.size shouldBe order.items.size
    document.takeawayInfo shouldBe TakeawayInfoDocument(
      pickupTime = order.takeawayInfo.pickupTime,
      customerName = order.takeawayInfo.customerName,
    )
    document.deliveryInfo.shouldBeNull()
    document.tableInfo.shouldBeNull()
  }

  @Test
  fun `toNullableDomain should map DELIVERY document to DeliveryOrder`() {
    val document = createDeliveryOrderDocument()

    val order = document.toNullableDomain()

    order.shouldNotBeNull()
    order.shouldBeInstanceOf<DeliveryOrder>()
    order as DeliveryOrder
    order.id.value shouldBe document.id
    order.restaurantId.value shouldBe document.restaurantId
    order.customerId.value shouldBe document.customerId
    order.status.name shouldBe document.status
    order.items.size shouldBe document.items.size
    order.deliveryInfo.estimatedDeliveryTime shouldBe document.deliveryInfo!!.estimatedDeliveryTime
    order.deliveryInfo.deliveryAddress shouldBe document.deliveryInfo!!.deliveryAddress
    order.deliveryInfo.bellName shouldBe document.deliveryInfo!!.bellName
    order.deliveryInfo.customerPhone shouldBe document.deliveryInfo!!.customerPhone
  }

  @Test
  fun `toNullableDomain should map DINE_IN document to DineInOrder`() {
    val document = createDineInOrderDocument()

    val order = document.toNullableDomain()

    order.shouldNotBeNull()
    order.shouldBeInstanceOf<DineInOrder>()
    order as DineInOrder
    order.id.value shouldBe document.id
    order.restaurantId.value shouldBe document.restaurantId
    order.customerId.value shouldBe document.customerId
    order.status.name shouldBe document.status
    order.items.size shouldBe document.items.size
    order.tableInfo.tableNumber shouldBe document.tableInfo!!.tableNumber
    order.tableInfo.numberOfGuests shouldBe document.tableInfo!!.numberOfGuests
  }

  @Test
  fun `toNullableDomain should map TAKEAWAY document to TakeawayOrder`() {
    val document = createTakeawayOrderDocument()

    val order = document.toNullableDomain()

    order.shouldNotBeNull()
    order.shouldBeInstanceOf<TakeawayOrder>()
    order as TakeawayOrder
    order.id.value shouldBe document.id
    order.restaurantId.value shouldBe document.restaurantId
    order.customerId.value shouldBe document.customerId
    order.status.name shouldBe document.status
    order.items.size shouldBe document.items.size
    order.takeawayInfo.pickupTime shouldBe document.takeawayInfo!!.pickupTime
    order.takeawayInfo.customerName shouldBe document.takeawayInfo!!.customerName
  }

  @Test
  fun `toNullableDomain should return null when deliveryInfo is missing for DELIVERY`() {
    val document = createDeliveryOrderDocument(deliveryInfo = null)

    val order = document.toNullableDomain()

    order.shouldBeNull()
  }

  @Test
  fun `toNullableDomain should return null when tableInfo is missing for DINE_IN`() {
    val document = createDineInOrderDocument(tableInfo = null)

    val order = document.toNullableDomain()

    order.shouldBeNull()
  }

  @Test
  fun `toNullableDomain should return null when takeawayInfo is missing for TAKEAWAY`() {
    val document = createTakeawayOrderDocument(takeawayInfo = null)

    val order = document.toNullableDomain()

    order.shouldBeNull()
  }

  @Test
  fun `toNullableDomain should return null when status is invalid`() {
    val document = createDeliveryOrderDocument(status = "NOT_A_REAL_STATUS")

    val order = document.toNullableDomain()

    order.shouldBeNull()
  }

  @Test
  fun `toNullableDomain should return null when orderType is unknown`() {
    val document = createDeliveryOrderDocument(orderType = "UNKNOWN_TYPE")

    val order = document.toNullableDomain()

    order.shouldBeNull()
  }
}
