package com.munchies.order.domain.model

import com.munchies.order.domain.model.Order.AdvanceStatusResult
import com.munchies.order.domain.model.Order.UpdateResult
import com.munchies.order.fixtures.createDineInOrder
import com.munchies.order.fixtures.createNewItems
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class DineInOrderTest {

  @Test
  fun `nextStatus of pending should be preparing`() {
    val order = createDineInOrder(OrderStatus.PENDING)
    val result = order.nextStatus()

    result.shouldBeInstanceOf<AdvanceStatusResult.Success>()
    result.order.status shouldBeEqual OrderStatus.PREPARING
  }

  @Test
  fun `nextStatus of preparing should be ready`() {
    val order = createDineInOrder(OrderStatus.PREPARING)
    val result = order.nextStatus()

    result.shouldBeInstanceOf<AdvanceStatusResult.Success>()
    result.order.status shouldBeEqual OrderStatus.READY
  }

  @Test
  fun `nextStatus of 'on the way' should be completed`() {
    val order = createDineInOrder(OrderStatus.READY)
    val result = order.nextStatus()

    result.shouldBeInstanceOf<AdvanceStatusResult.Success>()
    result.order.status shouldBeEqual OrderStatus.COMPLETED
  }

  @Test
  fun `nextStatus should fail when order is already COMPLETED`() {
    val order = createDineInOrder(OrderStatus.COMPLETED)
    val result = order.nextStatus()

    order.status shouldBeEqual OrderStatus.COMPLETED
    result shouldBeEqual AdvanceStatusResult.Failure.InvalidTransition
  }

  @Test
  fun `DineInOrder should update items`() {
    val order = createDineInOrder()
    val expectedItems = createNewItems()

    val result = order.updateItems(createNewItems())

    result.shouldBeInstanceOf<UpdateResult.Success>()
    result.order.items shouldBeEqual expectedItems
  }
}
