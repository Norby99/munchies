package com.munchies.order.application.usecase

import com.munchies.order.application.port.inbound.AdvanceOrderStatus
import com.munchies.order.application.port.inbound.command.AdvanceOrderStatusCommand
import com.munchies.order.domain.model.OrderStatus
import com.munchies.order.domain.ports.OrderRepository
import com.munchies.order.fixtures.createSampleOrder
import com.munchies.order.fixtures.defaultOrderId
import io.kotest.matchers.equals.shouldBeEqual
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class AdvanceOrderStatusUseCaseTest {

  private val repository = mockk<OrderRepository>(relaxed = false)
  private val useCase = AdvanceOrderStatusUseCase(repository)

  private val command = AdvanceOrderStatusCommand(defaultOrderId)

  @Test
  fun `execute should return OrderNotFound when order does not exist in repository`() {
    every { repository.findById(command.orderId) } returns null

    val result = useCase.execute(command)

    result shouldBeEqual AdvanceOrderStatus.Result.Failure.OrderNotFound
    verify(exactly = 0) { repository.update(any()) }
  }

  @Test
  fun `execute should return InvalidTransition when domain logic rejects the status advancement`() {
    val completedOrder = createSampleOrder(OrderStatus.COMPLETED)

    every { repository.findById(command.orderId) } returns completedOrder

    val result = useCase.execute(command)

    result shouldBeEqual AdvanceOrderStatus.Result.Failure.InvalidTransition
    verify(exactly = 0) { repository.update(any()) }
  }

  @Test
  fun `execute should update repository and return Success when transition is valid`() {
    val pendingOrder = createSampleOrder(OrderStatus.PENDING)

    every { repository.findById(command.orderId) } returns pendingOrder
    every { repository.update(any()) } returns Unit

    val result = useCase.execute(command)

    result shouldBeEqual AdvanceOrderStatus.Result.Success
    verify(exactly = 1) {
      repository.update(
        withArg { updatedOrder ->
          updatedOrder.status shouldBeEqual OrderStatus.PREPARING
          updatedOrder.id shouldBeEqual command.orderId
        },
      )
    }
  }
}
