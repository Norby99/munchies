package com.munchies.order.application.usecase

import com.munchies.order.application.port.inbound.DiscardOrder
import com.munchies.order.application.port.inbound.command.DiscardOrderCommand
import com.munchies.order.domain.model.OrderStatus
import com.munchies.order.domain.ports.OrderRepository
import com.munchies.order.fixtures.createSampleOrder
import com.munchies.order.fixtures.defaultOrderId
import io.kotest.matchers.equals.shouldBeEqual
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class DiscardOrderUseCaseUnitTest {

  private val repository = mockk<OrderRepository>(relaxed = false)
  private val useCase = DiscardOrderUseCase(repository)

  private val command = DiscardOrderCommand(defaultOrderId)

  @Test
  fun `execute should return OrderNotFound when order does not exist`() {
    every { repository.findById(command.orderId) } returns null

    val result = useCase.execute(command)

    result shouldBeEqual DiscardOrder.Result.Failure.OrderNotFound
    verify(exactly = 0) { repository.update(any()) }
  }

  @Test
  fun `execute should return OrderNotCancellable when order status is not PENDING`() {
    val nonCancellableOrder = createSampleOrder(OrderStatus.PREPARING)

    every { repository.findById(command.orderId) } returns nonCancellableOrder

    val result = useCase.execute(command)

    result shouldBeEqual DiscardOrder.Result.Failure.OrderNotCancellable
    verify(exactly = 0) { repository.update(any()) }
  }

  @Test
  fun `execute should update repository and return Success when order is pending`() {
    val cancellableOrder = createSampleOrder(OrderStatus.PENDING)

    every { repository.findById(command.orderId) } returns cancellableOrder
    every { repository.update(any()) } returns Unit

    val result = useCase.execute(command)

    result shouldBeEqual DiscardOrder.Result.Success

    verify(exactly = 1) {
      repository.update(
        withArg { updatedOrder ->
          updatedOrder.status shouldBeEqual OrderStatus.CANCELLED
          updatedOrder.id shouldBeEqual command.orderId
        },
      )
    }
  }
}
