package com.munchies.order.application.usecase

import com.munchies.order.application.port.inbound.PayOrder
import com.munchies.order.application.port.inbound.command.PayOrderCommand
import com.munchies.order.domain.model.OrderStatus
import com.munchies.order.domain.model.event.OrderPaid
import com.munchies.order.domain.port.OrderEventStore
import com.munchies.order.fixtures.asHistory
import com.munchies.order.fixtures.createSampleOrder
import com.munchies.order.fixtures.defaultOrderId
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class PayOrderUseCaseUnitTest {

  private val eventStore = mockk<OrderEventStore>(relaxed = false)
  private val useCase = PayOrderUseCase(eventStore)

  private val command = PayOrderCommand(defaultOrderId)

  @Test
  fun `execute should append OrderPaid and return Success when order is not paid yet`() {
    val history = createSampleOrder(OrderStatus.PENDING, payed = false).asHistory()
    every { eventStore.load(command.orderId) } returns history
    every { eventStore.append(any(), any(), any()) } just Runs

    val result = useCase.execute(command)

    result shouldBeEqual PayOrder.Result.Success
    verify(exactly = 1) {
      eventStore.append(
        command.orderId,
        history.size.toLong(),
        withArg { events -> events.single().shouldBeInstanceOf<OrderPaid>() },
      )
    }
  }

  @Test
  fun `execute should return AlreadyPaid when order is already paid`() {
    every { eventStore.load(command.orderId) } returns
      createSampleOrder(OrderStatus.PENDING, payed = true).asHistory()

    val result = useCase.execute(command)

    result shouldBeEqual PayOrder.Result.Failure.AlreadyPaid
    verify(exactly = 0) { eventStore.append(any(), any(), any()) }
  }

  @Test
  fun `execute should return OrderNotFound when order does not exist`() {
    every { eventStore.load(command.orderId) } returns emptyList()

    val result = useCase.execute(command)

    result shouldBeEqual PayOrder.Result.Failure.OrderNotFound
    verify(exactly = 0) { eventStore.append(any(), any(), any()) }
  }
}
