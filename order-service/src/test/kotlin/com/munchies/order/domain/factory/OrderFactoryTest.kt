package com.munchies.order.domain.factory

import com.munchies.order.domain.model.DeliveryOrder
import com.munchies.order.domain.model.OrderStatus
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class OrderFactoryTest {

  @Test
  fun `createDelivery should succeed with PENDING status when items and date are valid`() {
    val info = MockOrderFactory.createDeliveryInfo()

    val result = OrderFactory.createDelivery(
      MockOrderFactory.defaultOrderId,
      MockOrderFactory.defaultRestaurantId,
      MockOrderFactory.defaultCustomerId,
      MockOrderFactory.validItems,
      info,
    )

    result.shouldBeInstanceOf<OrderCreationResult.Success>()
    val order = result.order as DeliveryOrder
    order.status shouldBeEqual OrderStatus.PENDING
  }

  @Test
  fun `createDelivery should fail with InvalidDate when delivery time is in the past`() {
    val info =
      MockOrderFactory.createDeliveryInfo(estimatedDeliveryTime = MockOrderFactory.pastTime)

    val result = OrderFactory.createDelivery(
      MockOrderFactory.defaultOrderId,
      MockOrderFactory.defaultRestaurantId,
      MockOrderFactory.defaultCustomerId,
      MockOrderFactory.validItems,
      info,
    )

    result shouldBeEqual OrderCreationResult.Failure.InvalidDate
  }

  @Test
  fun `createDelivery should fail with EmptyItems when items list is empty`() {
    val info = MockOrderFactory.createDeliveryInfo()

    val result = OrderFactory.createDelivery(
      MockOrderFactory.defaultOrderId,
      MockOrderFactory.defaultRestaurantId,
      MockOrderFactory.defaultCustomerId,
      MockOrderFactory.emptyItems,
      info,
    )

    result shouldBeEqual OrderCreationResult.Failure.EmptyItems
  }

  @Test
  fun `createDineIn should succeed with PENDING status when items are valid`() {
    val info = MockOrderFactory.createTableInfo()

    val result = OrderFactory.createDineIn(
      MockOrderFactory.defaultOrderId,
      MockOrderFactory.defaultRestaurantId,
      MockOrderFactory.defaultCustomerId,
      MockOrderFactory.validItems,
      info,
    )

    result.shouldBeInstanceOf<OrderCreationResult.Success>()
  }

  @Test
  fun `createDelivery should fail with InvalidItemQuantity when an item is invalid`() {
    val info = MockOrderFactory.createDeliveryInfo()

    val result = OrderFactory.createDelivery(
      MockOrderFactory.defaultOrderId,
      MockOrderFactory.defaultRestaurantId,
      MockOrderFactory.defaultCustomerId,
      MockOrderFactory.invalidQuantityItems,
      info,
    )

    result shouldBeEqual OrderCreationResult.Failure.InvalidItemQuantity
  }

  @Test
  fun `isValidTime should succeed when pickup time is in the future`() {
    val result = OrderFactory.createTakeaway(
      MockOrderFactory.defaultOrderId,
      MockOrderFactory.defaultRestaurantId,
      MockOrderFactory.defaultCustomerId,
      MockOrderFactory.validItems,
      MockOrderFactory.createTakeawayInfo(pickupTime = MockOrderFactory.futureTime),
    )

    result.shouldBeInstanceOf<OrderCreationResult.Success>()
  }

  @Test
  fun `isValidTime should fail when pickup time is in the past`() {
    val result = OrderFactory.createTakeaway(
      MockOrderFactory.defaultOrderId,
      MockOrderFactory.defaultRestaurantId,
      MockOrderFactory.defaultCustomerId,
      MockOrderFactory.validItems,
      MockOrderFactory.createTakeawayInfo(pickupTime = MockOrderFactory.pastTime),
    )

    result.shouldBeInstanceOf<OrderCreationResult.Failure.InvalidDate>()
  }
}
