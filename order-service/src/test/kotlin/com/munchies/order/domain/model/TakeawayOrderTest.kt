package com.munchies.order.domain.model

import com.munchies.order.domain.model.Order.AdvanceStatusResult
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class TakeawayOrderTest {
  @Test
  fun `nextStatus of pending should be preparing`() {
    val order = defaultTakeawayOrder(OrderStatus.PENDING)
    val result = order.nextStatus()

    result.shouldBeInstanceOf<AdvanceStatusResult.Success>()
    result.order.status shouldBeEqual OrderStatus.PREPARING
  }

  @Test
  fun `nextStatus of preparing should be ready`() {
    val order = defaultTakeawayOrder(OrderStatus.PREPARING)
    val result = order.nextStatus()

    result.shouldBeInstanceOf<AdvanceStatusResult.Success>()
    result.order.status shouldBeEqual OrderStatus.READY
  }

  @Test
  fun `nextStatus of 'on the way' should be completed`() {
    val order = defaultTakeawayOrder(OrderStatus.READY)
    val result = order.nextStatus()

    result.shouldBeInstanceOf<AdvanceStatusResult.Success>()
    result.order.status shouldBeEqual OrderStatus.COMPLETED
  }

  @Test
  fun `nextStatus should fail when order is already COMPLETED`() {
    val order = defaultTakeawayOrder(OrderStatus.COMPLETED)
    val result = order.nextStatus()

    order.status shouldBeEqual OrderStatus.COMPLETED
    result shouldBeEqual AdvanceStatusResult.Failure.InvalidTransition
  }

  @Test
  fun `updateInfo should fail if estimated pickup time is in the past`() {
    val order = defaultTakeawayOrder()

    val result = order.updateInfo(PAST_TIME, Address2.bellName)

    result shouldBeEqual TakeawayOrder.UpdateResult.Failure.InvalidDate
  }

  @Test
  fun `updateInfo should succeed if data is valid`() {
    val order = defaultTakeawayOrder()

    val result = order.updateInfo(FUTURE_TIME, Address2.bellName)

    result.shouldBeInstanceOf<TakeawayOrder.UpdateResult.Success>()
    result.order.takeawayInfo.pickupTime shouldBeEqual FUTURE_TIME
    result.order.takeawayInfo.customerName shouldBeEqual Address2.bellName
  }
}
