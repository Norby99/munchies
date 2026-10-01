package com.munchies.order.application.usecase

import com.munchies.order.application.port.inbound.DiscardOrder
import com.munchies.order.application.port.inbound.command.DiscardOrderCommand
import com.munchies.order.domain.model.OrderStatus
import com.munchies.order.domain.model.event.OrderCancelled
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

class DiscardOrderUseCaseUnitTest {

  private val eventStore = mockk<OrderEventStore>(relaxed = false)
  private val useCase = DiscardOrderUseCase(eventStore)

  private val command = DiscardOrderCommand(defaultOrderId)

  @Test
  fun `execute should return OrderNotFound when order does not exist`() {
    every { eventStore.load(command.orderId) } returns emptyList()

    val result = useCase.execute(command)

    result shouldBeEqual DiscardOrder.Result.Failure.OrderNotFound
    verify(exactly = 0) { eventStore.append(any(), any(), any()) }
  }

  @Test
  fun `execute should return OrderNotCancellable when order status is not PENDING`() {
    every { eventStore.load(command.orderId) } returns
      createSampleOrder(OrderStatus.PREPARING).asHistory()

    val result = useCase.execute(command)

    result shouldBeEqual DiscardOrder.Result.Failure.OrderNotCancellable
    verify(exactly = 0) { eventStore.append(any(), any(), any()) }
  }

  @Test
  fun `execute should append OrderCancelled and return Success when order is pending`() {
    val history = createSampleOrder(OrderStatus.PENDING).asHistory()
    every { eventStore.load(command.orderId) } returns history
    every { eventStore.append(any(), any(), any()) } just Runs

    val result = useCase.execute(command)

    result shouldBeEqual DiscardOrder.Result.Success
    verify(exactly = 1) {
      eventStore.append(
        command.orderId,
        history.size.toLong(),
        withArg { events -> events.single().shouldBeInstanceOf<OrderCancelled>() },
      )
    }
  }
}
