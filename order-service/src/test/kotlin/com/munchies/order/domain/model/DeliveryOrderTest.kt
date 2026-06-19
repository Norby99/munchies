package com.munchies.order.domain.model

import io.kotest.matchers.equals.shouldBeEqual
import org.junit.jupiter.api.Test

class DeliveryOrderTest {

  @Test
  fun `nextStatus of pending should be preparing`() {
    val order = defaultDeliveryOrder(OrderStatus.PENDING)
    val result = order.nextStatus()

    val updatedOrder = (result as Order.AdvanceStatusResult.Success).order
    result shouldBeEqual Order.AdvanceStatusResult.Success(updatedOrder)
    updatedOrder.status shouldBeEqual OrderStatus.PREPARING
  }

  @Test
  fun `nextStatus of preparing should be ready`() {
    val order = defaultDeliveryOrder(OrderStatus.PREPARING)
    val result = order.nextStatus()

    val updatedOrder = (result as Order.AdvanceStatusResult.Success).order
    result shouldBeEqual Order.AdvanceStatusResult.Success(updatedOrder)
    updatedOrder.status shouldBeEqual OrderStatus.READY
  }

  @Test
  fun `nextStatus of ready should be 'on the way'`() {
    val order = defaultDeliveryOrder(OrderStatus.READY)
    val result = order.nextStatus()

    val updatedOrder = (result as Order.AdvanceStatusResult.Success).order
    result shouldBeEqual Order.AdvanceStatusResult.Success(updatedOrder)
    updatedOrder.status shouldBeEqual OrderStatus.ON_THE_WAY
  }

  @Test
  fun `nextStatus of 'on the way' should be completed`() {
    val order = defaultDeliveryOrder(OrderStatus.ON_THE_WAY)
    val result = order.nextStatus()

    val updatedOrder = (result as Order.AdvanceStatusResult.Success).order
    result shouldBeEqual Order.AdvanceStatusResult.Success(updatedOrder)
    updatedOrder.status shouldBeEqual OrderStatus.COMPLETED
  }

  @Test
  fun `nextStatus should fail when order is already COMPLETED`() {
    val order = defaultDeliveryOrder(OrderStatus.COMPLETED)
    val result = order.nextStatus()

    order.status shouldBeEqual OrderStatus.COMPLETED
    result shouldBeEqual Order.AdvanceStatusResult.Failure.InvalidTransition
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

    val updatedOrder = (result as DeliveryOrder.UpdateResult.Success).order
    updatedOrder.deliveryInfo.deliveryAddress shouldBeEqual Address2.deliveryAddress
    updatedOrder.deliveryInfo.bellName shouldBeEqual Address2.bellName
    updatedOrder.deliveryInfo.customerPhone shouldBeEqual Address2.customerPhone
    updatedOrder.deliveryInfo.estimatedDeliveryTime shouldBeEqual FUTURE_TIME
  }
}
