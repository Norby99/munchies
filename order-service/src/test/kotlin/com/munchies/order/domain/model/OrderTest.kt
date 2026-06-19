package com.munchies.order.domain.model

import com.munchies.order.domain.model.Order.UpdateResult
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class OrderTest {

  @Test
  fun `order should be cancellable only when in pending status`() {
    val order = defaultDeliveryOrder(OrderStatus.PENDING)
    val cancelledOrder = defaultDeliveryOrder(OrderStatus.CANCELLED)
    val cancelResult = order.cancel()
    cancelResult shouldBeEqual Order.CancelResult.Success(cancelledOrder)

    val orderPreparing = defaultDeliveryOrder(OrderStatus.PREPARING)
    val cancelResultPreparing = orderPreparing.cancel()
    cancelResultPreparing shouldBeEqual Order.CancelResult.Failure.InvalidTransition

    val orderReady = defaultDeliveryOrder(OrderStatus.READY)
    val cancelResultReady = orderReady.cancel()
    cancelResultReady shouldBeEqual Order.CancelResult.Failure.InvalidTransition

    val orderOnTheWay = defaultDeliveryOrder(OrderStatus.ON_THE_WAY)
    val cancelResultOnTheWay = orderOnTheWay.cancel()
    cancelResultOnTheWay shouldBeEqual Order.CancelResult.Failure.InvalidTransition

    val orderCompleted = defaultDeliveryOrder(OrderStatus.COMPLETED)
    val cancelResultCompleted = orderCompleted.cancel()
    cancelResultCompleted shouldBeEqual Order.CancelResult.Failure.InvalidTransition
  }

  @Test
  fun `order should update items`() {
    val order = defaultDeliveryOrder()
    val expectedItems = defualtNewItems()

    val result = order.updateItems(defualtNewItems())

    result.shouldBeInstanceOf<UpdateResult.Success>()
    result.order.items shouldBeEqual expectedItems
  }

  @Test
  fun `order should not update items when empty`() {
    val order = defaultDeliveryOrder()

    val result = order.updateItems(defaultEmptyItems())

    result.shouldBeInstanceOf<UpdateResult.Failure.InvalidItems>()
    result.error shouldBeEqual Order.ItemsValidationError.EmptyItems
  }

  @Test
  fun `order should not update items when item count is zero`() {
    val order = defaultDeliveryOrder()

    val result = order.updateItems(defaultInvalidItemsZeroCount())

    result.shouldBeInstanceOf<UpdateResult.Failure.InvalidItems>()
    result.error shouldBeEqual Order.ItemsValidationError.InvalidItemQuantity
  }

  @Test
  fun `order should not update items when item count is negative`() {
    val order = defaultDeliveryOrder()

    val result = order.updateItems(defaultInvalidItemsNegativeCount())

    result.shouldBeInstanceOf<UpdateResult.Failure.InvalidItems>()
    result.error shouldBeEqual Order.ItemsValidationError.InvalidItemQuantity
  }
}
