package com.munchies.order.domain.factory

import com.munchies.order.domain.model.DeliveryOrder
import com.munchies.order.domain.model.OrderStatus
import com.munchies.order.domain.model.event.OrderItemsUpdated
import com.munchies.order.domain.model.event.OrderPaid
import com.munchies.order.domain.model.event.TakeawayInfoUpdated
import com.munchies.order.fixtures.asHistory
import com.munchies.order.fixtures.createDeliveryInfo
import com.munchies.order.fixtures.createDeliveryOrder
import com.munchies.order.fixtures.createEmptyItems
import com.munchies.order.fixtures.createInvalidItemsZeroCount
import com.munchies.order.fixtures.createNewItems
import com.munchies.order.fixtures.createTakeawayInfo
import com.munchies.order.fixtures.createTakeawayOrder
import com.munchies.order.fixtures.defaultCustomerId
import com.munchies.order.fixtures.defaultOrderId
import com.munchies.order.fixtures.defaultRestaurantId
import com.munchies.order.fixtures.defaultTableInfo
import com.munchies.order.fixtures.futureTime
import com.munchies.order.fixtures.orderItem1
import com.munchies.order.fixtures.pastTime
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class OrderFactoryUnitTest {

  @Test
  fun `createDelivery should succeed with PENDING status when items and date are valid`() {
    val info = createDeliveryInfo()

    val result = OrderFactory.createDelivery(
      defaultOrderId,
      defaultRestaurantId,
      defaultCustomerId,
      createNewItems(),
      info,
    )

    result.shouldBeInstanceOf<OrderCreationResult.Success>()
    val order = OrderFactory.fromHistory(result.events) as DeliveryOrder
    order.status shouldBeEqual OrderStatus.PENDING
  }

  @Test
  fun `createDelivery should fail with InvalidDate when delivery time is in the past`() {
    val info = createDeliveryInfo(estimatedDeliveryTime = pastTime)

    val result = OrderFactory.createDelivery(
      defaultOrderId,
      defaultRestaurantId,
      defaultCustomerId,
      createNewItems(),
      info,
    )

    result shouldBeEqual OrderCreationResult.Failure.InvalidDate
  }

  @Test
  fun `createDelivery should fail with EmptyItems when items list is empty`() {
    val info = createDeliveryInfo()

    val result = OrderFactory.createDelivery(
      defaultOrderId,
      defaultRestaurantId,
      defaultCustomerId,
      createEmptyItems(),
      info,
    )

    result shouldBeEqual OrderCreationResult.Failure.EmptyItems
  }

  @Test
  fun `createDelivery should fail with InvalidItemQuantity when an item is invalid`() {
    val info = createDeliveryInfo()

    val result = OrderFactory.createDelivery(
      defaultOrderId,
      defaultRestaurantId,
      defaultCustomerId,
      createInvalidItemsZeroCount(),
      info,
    )

    result shouldBeEqual OrderCreationResult.Failure.InvalidItemQuantity
  }

  @Test
  fun `createDineIn should succeed with PENDING status when items are valid`() {
    val info = defaultTableInfo()

    val result = OrderFactory.createDineIn(
      defaultOrderId,
      defaultRestaurantId,
      defaultCustomerId,
      createNewItems(),
      info,
    )

    result.shouldBeInstanceOf<OrderCreationResult.Success>()
  }

  @Test
  fun `createDineIn should fail with EmptyItems when items list is invalid`() {
    val info = defaultTableInfo()

    val result = OrderFactory.createDineIn(
      defaultOrderId,
      defaultRestaurantId,
      defaultCustomerId,
      createInvalidItemsZeroCount(),
      info,
    )

    result shouldBeEqual OrderCreationResult.Failure.InvalidItemQuantity
  }

  @Test
  fun `createTakeaway should succeed with PENDING status when items and date are valid`() {
    val info = createTakeawayInfo(futureTime)

    val result = OrderFactory.createTakeaway(
      defaultOrderId,
      defaultRestaurantId,
      defaultCustomerId,
      createNewItems(),
      info,
    )

    result.shouldBeInstanceOf<OrderCreationResult.Success>()
  }

  @Test
  fun `createTakeaway should fail with EmptyItems when items list is empty`() {
    val info = createTakeawayInfo(futureTime)

    val result = OrderFactory.createTakeaway(
      defaultOrderId,
      defaultRestaurantId,
      defaultCustomerId,
      createEmptyItems(),
      info,
    )

    result shouldBeEqual OrderCreationResult.Failure.EmptyItems
  }

  @Test
  fun `createTakeaway should fail with InvalidItemQuantity when an item is invalid`() {
    val info = createTakeawayInfo(futureTime)

    val result = OrderFactory.createTakeaway(
      defaultOrderId,
      defaultRestaurantId,
      defaultCustomerId,
      createInvalidItemsZeroCount(),
      info,
    )

    result shouldBeEqual OrderCreationResult.Failure.InvalidItemQuantity
  }

  @Test
  fun `createTakeaway should fail when pickup time is in the past`() {
    val info = createTakeawayInfo(pastTime)

    val result = OrderFactory.createTakeaway(
      defaultOrderId,
      defaultRestaurantId,
      defaultCustomerId,
      createNewItems(),
      info,
    )

    result shouldBeEqual OrderCreationResult.Failure.InvalidDate
  }

  // ---------- Rehydration from the event stream ----------

  @Test
  fun `fromHistory should return null for an empty stream`() {
    OrderFactory.fromHistory(emptyList()).shouldBeNull()
  }

  @Test
  fun `fromHistory should return null when the stream does not start with OrderPlaced`() {
    OrderFactory.fromHistory(listOf(OrderPaid(defaultOrderId))).shouldBeNull()
  }

  @Test
  fun `fromHistory should replay every event in order`() {
    val expected = createDeliveryOrder(status = OrderStatus.ON_THE_WAY).copy(payed = true)

    val rebuilt = OrderFactory.fromHistory(expected.asHistory())

    rebuilt shouldBe expected
  }

  @Test
  fun `fromHistory should apply item and info updates`() {
    val placed = createTakeawayOrder()
    val newInfo = createTakeawayInfo(customerName = "Verdi")
    val history = placed.asHistory() + listOf(
      OrderItemsUpdated(defaultOrderId, listOf(orderItem1)),
      TakeawayInfoUpdated(defaultOrderId, newInfo),
    )

    val rebuilt = OrderFactory.fromHistory(history)

    rebuilt shouldBe placed.copy(items = listOf(orderItem1), takeawayInfo = newInfo)
  }
}
