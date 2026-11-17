package com.munchies.order.application.usecase

import com.munchies.order.application.port.inbound.UpdateOrderItems
import com.munchies.order.domain.model.CustomerId
import com.munchies.order.domain.model.OrderStatus
import com.munchies.order.domain.model.event.OrderItemsUpdated
import com.munchies.order.domain.port.MenuCatalog
import com.munchies.order.domain.port.OrderEventStore
import com.munchies.order.fixtures.asHistory
import com.munchies.order.fixtures.createEmptyItems
import com.munchies.order.fixtures.createInvalidItemsZeroCount
import com.munchies.order.fixtures.createNewItems
import com.munchies.order.fixtures.createSampleOrder
import com.munchies.order.fixtures.createUpdateOrderItemsCommand
import com.munchies.order.fixtures.menuPrices
import com.munchies.order.fixtures.requested
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class UpdateOrderItemsUseCaseUnitTest {

  private val eventStore = mockk<OrderEventStore>(relaxed = false)
  private val menuCatalog = mockk<MenuCatalog> {
    every { findPrices(any(), any()) } returns MenuCatalog.PriceLookup.Found(menuPrices())
  }
  private val useCase = UpdateOrderItemsUseCase(eventStore, menuCatalog)

  @Test
  fun `execute should return OrderNotFound when order does not exist`() {
    val command = createUpdateOrderItemsCommand()
    every { eventStore.load(command.orderId) } returns emptyList()

    val result = useCase.execute(command)

    result shouldBeEqual UpdateOrderItems.Result.Failure.OrderNotFound
    verify(exactly = 0) { eventStore.append(any(), any(), any()) }
  }

  @Test
  fun `execute should return Unauthorized when order belongs to a different customer`() {
    val command = createUpdateOrderItemsCommand()
    every { eventStore.load(command.orderId) } returns createSampleOrder(OrderStatus.PENDING)
      .copy(customerId = CustomerId("another-customer-999"))
      .asHistory()

    val result = useCase.execute(command)

    result shouldBeEqual UpdateOrderItems.Result.Failure.Unauthorized
    verify(exactly = 0) { eventStore.append(any(), any(), any()) }
  }

  @Test
  fun `execute should return EmptyItems when the command contains an empty list of items`() {
    val command = createUpdateOrderItemsCommand(items = requested(createEmptyItems()))
    every { eventStore.load(command.orderId) } returns
      createSampleOrder(OrderStatus.PENDING).asHistory()

    val result = useCase.execute(command)

    result shouldBeEqual UpdateOrderItems.Result.Failure.EmptyItems
    verify(exactly = 0) { eventStore.append(any(), any(), any()) }
  }

  @Test
  fun `execute should return EmptyItems when the command contains items with invalid quantity`() {
    val command = createUpdateOrderItemsCommand(items = requested(createInvalidItemsZeroCount()))
    every { eventStore.load(command.orderId) } returns
      createSampleOrder(OrderStatus.PENDING).asHistory()

    val result = useCase.execute(command)

    result shouldBeEqual UpdateOrderItems.Result.Failure.EmptyItems
    verify(exactly = 0) { eventStore.append(any(), any(), any()) }
  }

  @Test
  fun `execute should return OrderNotModifiable when order has left the pending status`() {
    val command = createUpdateOrderItemsCommand()
    every { eventStore.load(command.orderId) } returns
      createSampleOrder(OrderStatus.PREPARING).asHistory()

    val result = useCase.execute(command)

    result shouldBeEqual UpdateOrderItems.Result.Failure.OrderNotModifiable
    verify(exactly = 0) { eventStore.append(any(), any(), any()) }
    verify(exactly = 0) { menuCatalog.findPrices(any(), any()) }
  }

  @Test
  fun `execute should append OrderItemsUpdated and return Success when command is valid`() {
    val newItems = createNewItems()
    val command = createUpdateOrderItemsCommand(items = requested(newItems))
    val history = createSampleOrder(OrderStatus.PENDING).asHistory()
    every { eventStore.load(command.orderId) } returns history
    every { eventStore.append(any(), any(), any()) } just Runs

    val result = useCase.execute(command)

    result shouldBeEqual UpdateOrderItems.Result.Success
    verify(exactly = 1) {
      eventStore.append(
        command.orderId,
        history.size.toLong(),
        withArg { events ->
          val event = events.single()
          event.shouldBeInstanceOf<OrderItemsUpdated>()
          event.items shouldBeEqual newItems
        },
      )
    }
  }

  @Test
  fun `execute prices the new items against the order's restaurant`() {
    val order = createSampleOrder(OrderStatus.PENDING)
    every { eventStore.load(any()) } returns order.asHistory()
    every { eventStore.append(any(), any(), any()) } just Runs

    useCase.execute(createUpdateOrderItemsCommand())

    verify(exactly = 1) {
      menuCatalog.findPrices(order.restaurantId, createNewItems().map { it.menuItemId }.toSet())
    }
  }

  @Test
  fun `execute should return UnknownItems when an item is not on the menus`() {
    every { eventStore.load(any()) } returns createSampleOrder(OrderStatus.PENDING).asHistory()
    every { menuCatalog.findPrices(any(), any()) } returns
      MenuCatalog.PriceLookup.Found(menuPrices(listOf(createNewItems().first())))

    val result = useCase.execute(createUpdateOrderItemsCommand())

    result shouldBeEqual UpdateOrderItems.Result.Failure.UnknownItems(
      setOf(createNewItems().last().menuItemId),
    )
    verify(exactly = 0) { eventStore.append(any(), any(), any()) }
  }

  @Test
  fun `execute should return MenuUnavailable when the prices cannot be read`() {
    every { eventStore.load(any()) } returns createSampleOrder(OrderStatus.PENDING).asHistory()
    every { menuCatalog.findPrices(any(), any()) } returns MenuCatalog.PriceLookup.Unavailable

    val result = useCase.execute(createUpdateOrderItemsCommand())

    result shouldBeEqual UpdateOrderItems.Result.Failure.MenuUnavailable
    verify(exactly = 0) { eventStore.append(any(), any(), any()) }
  }
}
