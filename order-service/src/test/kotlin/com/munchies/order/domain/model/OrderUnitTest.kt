package com.munchies.order.domain.model

import com.munchies.order.domain.model.Order.UpdateResult
import com.munchies.order.domain.model.event.DeliveryInfoUpdated
import com.munchies.order.domain.model.event.OrderPaid
import com.munchies.order.domain.model.event.OrderPlaced
import com.munchies.order.domain.model.event.OrderStatusAdvanced
import com.munchies.order.fixtures.after
import com.munchies.order.fixtures.createDeliveryInfo
import com.munchies.order.fixtures.createDeliveryOrder
import com.munchies.order.fixtures.createDineInOrder
import com.munchies.order.fixtures.createEmptyItems
import com.munchies.order.fixtures.createInvalidItemsNegativeCount
import com.munchies.order.fixtures.createInvalidItemsZeroCount
import com.munchies.order.fixtures.createNewItems
import com.munchies.order.fixtures.createTakeawayInfo
import com.munchies.order.fixtures.createTakeawayOrder
import com.munchies.order.fixtures.defaultCustomerId
import com.munchies.order.fixtures.defaultOrderId
import com.munchies.order.fixtures.defaultRestaurantId
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class OrderUnitTest {

  @Test
  fun `order should be cancellable only when in pending status`() {
    val order = createDeliveryOrder(OrderStatus.PENDING)
    val cancelResult = order.cancel()
    cancelResult.shouldBeInstanceOf<Order.CancelResult.Success>()
    order.after(cancelResult.events) shouldBeEqual createDeliveryOrder(OrderStatus.CANCELLED)

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
    val cancelledOrder = order.after(result.events)

    cancelledOrder.status shouldBeEqual OrderStatus.CANCELLED
  }

  @Test
  fun `order should update items`() {
    val order = createDeliveryOrder()
    val expectedItems = createNewItems()

    val result = order.updateItems(createNewItems())

    result.shouldBeInstanceOf<UpdateResult.Success>()
    order.after(result.events).items shouldBeEqual expectedItems
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

  // ---------- Event sourcing: process never mutates, apply never fails ----------

  @Test
  fun `process methods should return events without mutating the order`() {
    val order = createDeliveryOrder(OrderStatus.PENDING)

    val result = order.nextStatus()

    result.shouldBeInstanceOf<Order.AdvanceStatusResult.Success>()
    result.events shouldBeEqual listOf(
      OrderStatusAdvanced(
        order.id,
        OrderStatus.PENDING,
        OrderStatus.PREPARING,
        result.events.single().occurredAt,
      ),
    )
    order.status shouldBeEqual OrderStatus.PENDING
  }

  @Test
  fun `pay should emit OrderPaid and fail when already paid`() {
    val order = createDeliveryOrder()

    val result = order.pay()

    result.shouldBeInstanceOf<Order.PayResult.Success>()
    result.events.single().shouldBeInstanceOf<OrderPaid>()
    val paid = order.after(result.events)
    paid.payed shouldBeEqual true
    paid.pay() shouldBeEqual Order.PayResult.Failure.AlreadyPaid
  }

  @Test
  fun `apply should ignore events that do not concern the order subtype`() {
    val order = createDineInOrder()

    val applied = order.apply(DeliveryInfoUpdated(order.id, createDeliveryInfo()))

    applied shouldBeEqual order
  }

  @Test
  fun `from should build the subtype matching the details of OrderPlaced`() {
    val placed = OrderPlaced(
      defaultOrderId,
      defaultRestaurantId,
      defaultCustomerId,
      createNewItems(),
      createTakeawayInfo(),
    )

    val order = Order.from(placed)

    order shouldBeEqual createTakeawayOrder()
  }
}
