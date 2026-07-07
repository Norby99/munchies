package com.munchies.order.domain.model

import com.munchies.order.domain.model.Order.UpdateResult
import com.munchies.order.fixtures.createDeliveryOrder
import com.munchies.order.fixtures.createDineInOrder
import com.munchies.order.fixtures.createEmptyItems
import com.munchies.order.fixtures.createInvalidItemsNegativeCount
import com.munchies.order.fixtures.createInvalidItemsZeroCount
import com.munchies.order.fixtures.createNewItems
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class OrderTest {

  @Test
  fun `order should be cancellable only when in pending status`() {
    val order = createDeliveryOrder(OrderStatus.PENDING)
    val cancelledOrder = createDeliveryOrder(OrderStatus.CANCELLED)
    val cancelResult = order.cancel()
    cancelResult shouldBeEqual Order.CancelResult.Success(cancelledOrder)

    val orderPreparing = createDeliveryOrder(OrderStatus.PREPARING)
    val cancelResultPreparing = orderPreparing.cancel()
    cancelResultPreparing shouldBeEqual Order.CancelResult.Failure.InvalidTransition

    val orderReady = createDeliveryOrder(OrderStatus.READY)
    val cancelResultReady = orderReady.cancel()
    cancelResultReady shouldBeEqual Order.CancelResult.Failure.InvalidTransition

    val orderOnTheWay = createDeliveryOrder(OrderStatus.ON_THE_WAY)
    val cancelResultOnTheWay = orderOnTheWay.cancel()
    cancelResultOnTheWay shouldBeEqual Order.CancelResult.Failure.InvalidTransition

    val orderCompleted = createDeliveryOrder(OrderStatus.COMPLETED)
    val cancelResultCompleted = orderCompleted.cancel()
    cancelResultCompleted shouldBeEqual Order.CancelResult.Failure.InvalidTransition
  }

  @Test
  fun `cancel should succeed and update status to CANCELLED when order is PENDING`() {
    val order = createDineInOrder(OrderStatus.PENDING)

    val result = order.cancel()

    result.shouldBeInstanceOf<Order.CancelResult.Success>()
    val cancelledOrder = result.order

    cancelledOrder.status shouldBeEqual OrderStatus.CANCELLED
  }

  @Test
  fun `order should update items`() {
    val order = createDeliveryOrder()
    val expectedItems = createNewItems()

    val result = order.updateItems(createNewItems())

    result.shouldBeInstanceOf<UpdateResult.Success>()
    result.order.items shouldBeEqual expectedItems
  }

  @Test
  fun `order should not update items when empty`() {
    val order = createDeliveryOrder()

    val result = order.updateItems(createEmptyItems())

    result.shouldBeInstanceOf<UpdateResult.Failure.InvalidItems>()
    result.error shouldBeEqual Order.ItemsValidationError.EmptyItems
  }

  @Test
  fun `order should not update items when item count is zero`() {
    val order = createDeliveryOrder()

    val result = order.updateItems(createInvalidItemsZeroCount())

    result.shouldBeInstanceOf<UpdateResult.Failure.InvalidItems>()
    result.error shouldBeEqual Order.ItemsValidationError.InvalidItemQuantity
  }

  @Test
  fun `order should not update items when item count is negative`() {
    val order = createDeliveryOrder()

    val result = order.updateItems(createInvalidItemsNegativeCount())

    result.shouldBeInstanceOf<UpdateResult.Failure.InvalidItems>()
    result.error shouldBeEqual Order.ItemsValidationError.InvalidItemQuantity
  }
}
