package com.munchies.order.application.usecase

import com.munchies.order.application.port.inbound.UpdateOrderItems
import com.munchies.order.domain.model.CustomerId
import com.munchies.order.domain.model.OrderStatus
import com.munchies.order.domain.ports.OrderRepository
import com.munchies.order.fixtures.createEmptyItems
import com.munchies.order.fixtures.createInvalidItemsZeroCount
import com.munchies.order.fixtures.createNewItems
import com.munchies.order.fixtures.createSampleOrder
import com.munchies.order.fixtures.createUpdateOrderItemsCommand
import io.kotest.matchers.equals.shouldBeEqual
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class UpdateOrderItemsUseCaseUnitTest {

  private val repository = mockk<OrderRepository>(relaxed = false)
  private val useCase = UpdateOrderItemsUseCase(repository)

  @Test
  fun `execute should return OrderNotFound when order does not exist`() {
    val command = createUpdateOrderItemsCommand()
    every { repository.findById(command.orderId) } returns null

    val result = useCase.execute(command)

    result shouldBeEqual UpdateOrderItems.Result.Failure.OrderNotFound
    verify(exactly = 0) { repository.update(any()) }
  }

  @Test
  fun `execute should return Unauthorized when order belongs to a different customer`() {
    val command = createUpdateOrderItemsCommand()
    val wrongCustomer = CustomerId("another-customer-999")
    val orderOfAnotherCustomer = createSampleOrder(OrderStatus.PENDING)
      .copy(customerId = wrongCustomer)

    every { repository.findById(command.orderId) } returns orderOfAnotherCustomer

    val result = useCase.execute(command)

    result shouldBeEqual UpdateOrderItems.Result.Failure.Unauthorized
    verify(exactly = 0) { repository.update(any()) }
  }

  @Test
  fun `execute should return EmptyItems when the command contains an empty list of items`() {
    val command = createUpdateOrderItemsCommand(items = createEmptyItems())
    val existingOrder = createSampleOrder(OrderStatus.PENDING)

    every { repository.findById(command.orderId) } returns existingOrder

    val result = useCase.execute(command)

    result shouldBeEqual UpdateOrderItems.Result.Failure.EmptyItems
    verify(exactly = 0) { repository.update(any()) }
  }

  @Test
  fun `execute should update repository and return Success when command is valid`() {
    val newItems = createNewItems()
    val command = createUpdateOrderItemsCommand(items = newItems)

    val existingOrder = createSampleOrder(OrderStatus.PENDING)

    every { repository.findById(command.orderId) } returns existingOrder
    every { repository.update(any()) } returns Unit

    val result = useCase.execute(command)

    result shouldBeEqual UpdateOrderItems.Result.Success

    verify(exactly = 1) {
      repository.update(
        withArg { updatedOrder ->
          updatedOrder.id shouldBeEqual command.orderId
          updatedOrder.items shouldBeEqual newItems
        },
      )
    }
  }

  @Test
  fun `execute should return EmptyItems when the command contains items with invalid quantity`() {
    val command = createUpdateOrderItemsCommand(items = createInvalidItemsZeroCount())
    val existingOrder = createSampleOrder(OrderStatus.PENDING)

    every { repository.findById(command.orderId) } returns existingOrder

    val result = useCase.execute(command)

    result shouldBeEqual UpdateOrderItems.Result.Failure.EmptyItems

    verify(exactly = 0) { repository.update(any()) }
  }
}
