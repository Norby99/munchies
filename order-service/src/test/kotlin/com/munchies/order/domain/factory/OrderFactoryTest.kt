package com.munchies.order.domain.factory

import com.munchies.order.domain.model.DeliveryOrder
import com.munchies.order.domain.model.OrderStatus
import com.munchies.order.fixtures.defaultCustomerId
import com.munchies.order.fixtures.defaultDeliveryInfo
import com.munchies.order.fixtures.defaultEmptyItems
import com.munchies.order.fixtures.defaultInvalidItemsZeroCount
import com.munchies.order.fixtures.defaultNewItems
import com.munchies.order.fixtures.defaultOrderId
import com.munchies.order.fixtures.defaultRestaurantId
import com.munchies.order.fixtures.defaultTableInfo
import com.munchies.order.fixtures.defaultTakeawayInfo
import com.munchies.order.fixtures.futureTime
import com.munchies.order.fixtures.pastTime
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class OrderFactoryTest {

  @Test
  fun `createDelivery should succeed with PENDING status when items and date are valid`() {
    val info = defaultDeliveryInfo()

    val result = OrderFactory.createDelivery(
      defaultOrderId,
      defaultRestaurantId,
      defaultCustomerId,
      defaultNewItems(),
      info,
    )

    result.shouldBeInstanceOf<OrderCreationResult.Success>()
    val order = result.order as DeliveryOrder
    order.status shouldBeEqual OrderStatus.PENDING
  }

  @Test
  fun `createDelivery should fail with InvalidDate when delivery time is in the past`() {
    val info = defaultDeliveryInfo(estimatedDeliveryTime = pastTime)

    val result = OrderFactory.createDelivery(
      defaultOrderId,
      defaultRestaurantId,
      defaultCustomerId,
      defaultNewItems(),
      info,
    )

    result shouldBeEqual OrderCreationResult.Failure.InvalidDate
  }

  @Test
  fun `createDelivery should fail with EmptyItems when items list is empty`() {
    val info = defaultDeliveryInfo()

    val result = OrderFactory.createDelivery(
      defaultOrderId,
      defaultRestaurantId,
      defaultCustomerId,
      defaultEmptyItems(),
      info,
    )

    result shouldBeEqual OrderCreationResult.Failure.EmptyItems
  }

  @Test
  fun `createDineIn should succeed with PENDING status when items are valid`() {
    val info = defaultTableInfo()

    val result = OrderFactory.createDineIn(
      defaultOrderId,
      defaultRestaurantId,
      defaultCustomerId,
      defaultNewItems(),
      info,
    )

    result.shouldBeInstanceOf<OrderCreationResult.Success>()
  }

  @Test
  fun `createDelivery should fail with InvalidItemQuantity when an item is invalid`() {
    val info = defaultDeliveryInfo()

    val result = OrderFactory.createDelivery(
      defaultOrderId,
      defaultRestaurantId,
      defaultCustomerId,
      defaultInvalidItemsZeroCount(),
      info,
    )

    result shouldBeEqual OrderCreationResult.Failure.InvalidItemQuantity
  }

  @Test
  fun `isValidTime should succeed when pickup time is in the future`() {
    val info = defaultTakeawayInfo(futureTime)

    val result = OrderFactory.createTakeaway(
      defaultOrderId,
      defaultRestaurantId,
      defaultCustomerId,
      defaultNewItems(),
      info,
    )

    result.shouldBeInstanceOf<OrderCreationResult.Success>()
  }

  @Test
  fun `isValidTime should fail when pickup time is in the past`() {
    val info = defaultTakeawayInfo(pastTime)

    val result = OrderFactory.createTakeaway(
      defaultOrderId,
      defaultRestaurantId,
      defaultCustomerId,
      defaultNewItems(),
      info,
    )

    result.shouldBeInstanceOf<OrderCreationResult.Failure.InvalidDate>()
  }
}
