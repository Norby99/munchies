package com.munchies.order.application.usecase

import com.munchies.order.application.port.inbound.UpdateOrderItems
import com.munchies.order.domain.model.CustomerId
import com.munchies.order.domain.model.OrderStatus
import com.munchies.order.domain.model.event.OrderItemsUpdated
import com.munchies.order.domain.port.OrderEventStore
import com.munchies.order.fixtures.asHistory
import com.munchies.order.fixtures.createEmptyItems
import com.munchies.order.fixtures.createInvalidItemsZeroCount
import com.munchies.order.fixtures.createNewItems
import com.munchies.order.fixtures.createSampleOrder
import com.munchies.order.fixtures.createUpdateOrderItemsCommand
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
  private val useCase = UpdateOrderItemsUseCase(eventStore)

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
    val command = createUpdateOrderItemsCommand(items = createEmptyItems())
    every { eventStore.load(command.orderId) } returns
      createSampleOrder(OrderStatus.PENDING).asHistory()

    val result = useCase.execute(command)

    result shouldBeEqual UpdateOrderItems.Result.Failure.EmptyItems
    verify(exactly = 0) { eventStore.append(any(), any(), any()) }
  }

  @Test
  fun `execute should return EmptyItems when the command contains items with invalid quantity`() {
    val command = createUpdateOrderItemsCommand(items = createInvalidItemsZeroCount())
    every { eventStore.load(command.orderId) } returns
      createSampleOrder(OrderStatus.PENDING).asHistory()

    val result = useCase.execute(command)

    result shouldBeEqual UpdateOrderItems.Result.Failure.EmptyItems
    verify(exactly = 0) { eventStore.append(any(), any(), any()) }
  }

  @Test
  fun `execute should append OrderItemsUpdated and return Success when command is valid`() {
    val newItems = createNewItems()
    val command = createUpdateOrderItemsCommand(items = newItems)
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
}
