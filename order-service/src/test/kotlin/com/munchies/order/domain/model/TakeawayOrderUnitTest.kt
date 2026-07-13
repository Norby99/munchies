package com.munchies.order.domain.model

import com.munchies.order.domain.model.Order.AdvanceStatusResult
import com.munchies.order.fixtures.Address2
import com.munchies.order.fixtures.createTakeawayOrder
import com.munchies.order.fixtures.futureTime
import com.munchies.order.fixtures.pastTime
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class TakeawayOrderUnitTest {
  @Test
  fun `nextStatus of pending should be preparing`() {
    val order = createTakeawayOrder(OrderStatus.PENDING)
    val result = order.nextStatus()

    result.shouldBeInstanceOf<AdvanceStatusResult.Success>()
    result.order.status shouldBeEqual OrderStatus.PREPARING
  }

  @Test
  fun `nextStatus of preparing should be ready`() {
    val order = createTakeawayOrder(OrderStatus.PREPARING)
    val result = order.nextStatus()

    result.shouldBeInstanceOf<AdvanceStatusResult.Success>()
    result.order.status shouldBeEqual OrderStatus.READY
  }

  @Test
  fun `nextStatus of 'on the way' should be completed`() {
    val order = createTakeawayOrder(OrderStatus.READY)
    val result = order.nextStatus()

    result.shouldBeInstanceOf<AdvanceStatusResult.Success>()
    result.order.status shouldBeEqual OrderStatus.COMPLETED
  }

  @Test
  fun `nextStatus should fail when order is already COMPLETED`() {
    val order = createTakeawayOrder(OrderStatus.COMPLETED)
    val result = order.nextStatus()

    order.status shouldBeEqual OrderStatus.COMPLETED
    result shouldBeEqual AdvanceStatusResult.Failure.InvalidTransition
  }

  @Test
  fun `updateInfo should fail if estimated pickup time is in the past`() {
    val order = createTakeawayOrder()

    val result = order.updateInfo(pastTime, Address2.bellName)

    result shouldBeEqual TakeawayOrder.UpdateResult.Failure.InvalidDate
  }

  @Test
  fun `updateInfo should succeed if data is valid`() {
    val order = createTakeawayOrder()

    val result = order.updateInfo(futureTime, Address2.bellName)

    result.shouldBeInstanceOf<TakeawayOrder.UpdateResult.Success>()
    result.order.takeawayInfo.pickupTime shouldBeEqual futureTime
    result.order.takeawayInfo.customerName shouldBeEqual Address2.bellName
  }
}
