package com.munchies.order.domain.model

import com.munchies.order.domain.model.Order.AdvanceStatusResult
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class DeliveryOrderTest {

  @Test
  fun `nextStatus of pending should be preparing`() {
    val order = defaultDeliveryOrder(OrderStatus.PENDING)
    val result = order.nextStatus()

    result.shouldBeInstanceOf<AdvanceStatusResult.Success>()
    result.order.status shouldBeEqual OrderStatus.PREPARING
  }

  @Test
  fun `nextStatus of preparing should be ready`() {
    val order = defaultDeliveryOrder(OrderStatus.PREPARING)
    val result = order.nextStatus()

    result.shouldBeInstanceOf<AdvanceStatusResult.Success>()
    result.order.status shouldBeEqual OrderStatus.READY
  }

  @Test
  fun `nextStatus of ready should be 'on the way'`() {
    val order = defaultDeliveryOrder(OrderStatus.READY)
    val result = order.nextStatus()

    result.shouldBeInstanceOf<AdvanceStatusResult.Success>()
    result.order.status shouldBeEqual OrderStatus.ON_THE_WAY
  }

  @Test
  fun `nextStatus of 'on the way' should be completed`() {
    val order = defaultDeliveryOrder(OrderStatus.ON_THE_WAY)
    val result = order.nextStatus()

    result.shouldBeInstanceOf<AdvanceStatusResult.Success>()
    result.order.status shouldBeEqual OrderStatus.COMPLETED
  }

  @Test
  fun `nextStatus should fail when order is already COMPLETED`() {
    val order = defaultDeliveryOrder(OrderStatus.COMPLETED)
    val result = order.nextStatus()

    order.status shouldBeEqual OrderStatus.COMPLETED
    result shouldBeEqual AdvanceStatusResult.Failure.InvalidTransition
  }

  @Test
  fun `updateInfo should fail if estimated delivery time is in the past`() {
    val order = defaultDeliveryOrder()

    val result = order.updateInfo(
      PAST_TIME,
      Address2.deliveryAddress,
      Address2.bellName,
      Address2.customerPhone,
    )

    result shouldBeEqual DeliveryOrder.UpdateResult.Failure.InvalidDate
  }

  @Test
  fun `updateInfo should succeed if data is valid`() {
    val order = defaultDeliveryOrder()

    val result = order.updateInfo(
      FUTURE_TIME,
      Address2.deliveryAddress,
      Address2.bellName,
      Address2.customerPhone,
    )

    result.shouldBeInstanceOf<DeliveryOrder.UpdateResult.Success>()
    result.order.deliveryInfo.deliveryAddress shouldBeEqual Address2.deliveryAddress
    result.order.deliveryInfo.bellName shouldBeEqual Address2.bellName
    result.order.deliveryInfo.customerPhone shouldBeEqual Address2.customerPhone
    result.order.deliveryInfo.estimatedDeliveryTime shouldBeEqual FUTURE_TIME
  }
}
