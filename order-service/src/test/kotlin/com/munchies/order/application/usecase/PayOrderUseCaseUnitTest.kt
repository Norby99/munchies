package com.munchies.order.application.usecase

import com.munchies.order.application.port.inbound.PayOrder
import com.munchies.order.application.port.inbound.command.PayOrderCommand
import com.munchies.order.domain.ports.OrderRepository
import com.munchies.order.fixtures.createSampleOrder
import com.munchies.order.fixtures.defaultOrderId
import io.kotest.matchers.equals.shouldBeEqual
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class PayOrderUseCaseUnitTest {

  private val repository = mockk<OrderRepository>(relaxed = false)
  private val useCase = PayOrderUseCase(repository)

  private val command = PayOrderCommand(defaultOrderId)

  @Test
  fun `execute should update repository and return Success when order is payed`() {
    val unpaidOrder = createSampleOrder(payed = false)

    every { repository.findById(command.orderId) } returns unpaidOrder
    every { repository.update(any()) } returns Unit

    val result = useCase.execute(command)

    result shouldBeEqual PayOrder.Result.Success
    verify(exactly = 1) {
      repository.update(
        withArg { updatedOrder ->
          updatedOrder.payed shouldBeEqual true
          updatedOrder.id shouldBeEqual command.orderId
        },
      )
    }
  }

  @Test
  fun `execute should return AlreadyPaid when order is already paid`() {
    val paidOrder = createSampleOrder(payed = true)

    every { repository.findById(command.orderId) } returns paidOrder

    val result = useCase.execute(command)

    result shouldBeEqual PayOrder.Result.Failure.AlreadyPaid
    verify(exactly = 0) { repository.update(any()) }
  }

  @Test
  fun `execute should return OrderNotFound when order does not exist in repository`() {
    every { repository.findById(command.orderId) } returns null

    val result = useCase.execute(command)

    result shouldBeEqual PayOrder.Result.Failure.OrderNotFound
    verify(exactly = 0) { repository.update(any()) }
  }
}
