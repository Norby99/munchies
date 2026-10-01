package com.munchies.order.application.usecase

import com.munchies.order.application.port.inbound.AdvanceOrderStatus
import com.munchies.order.application.port.inbound.command.AdvanceOrderStatusCommand
import com.munchies.order.domain.model.OrderStatus
import com.munchies.order.domain.model.event.OrderStatusAdvanced
import com.munchies.order.domain.port.OrderEventStore
import com.munchies.order.domain.port.OrderNotificationPublisher
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
import io.mockk.verifyOrder
import org.junit.jupiter.api.Test

class AdvanceOrderStatusUseCaseUnitTest {

  private val eventStore = mockk<OrderEventStore>(relaxed = false)
  private val notificationPublisher = mockk<OrderNotificationPublisher>(relaxed = true)
  private val useCase = AdvanceOrderStatusUseCase(eventStore, notificationPublisher)

  private val command = AdvanceOrderStatusCommand(defaultOrderId)

  @Test
  fun `execute should return OrderNotFound when the order has no event stream`() {
    every { eventStore.load(command.orderId) } returns emptyList()

    val result = useCase.execute(command)

    result shouldBeEqual AdvanceOrderStatus.Result.Failure.OrderNotFound
    verify(exactly = 0) { eventStore.append(any(), any(), any()) }
    verify(exactly = 0) { notificationPublisher.publishStatusChanged(any()) }
  }

  @Test
  fun `execute should return InvalidTransition when domain logic rejects the status advancement`() {
    every { eventStore.load(command.orderId) } returns
      createSampleOrder(OrderStatus.COMPLETED).asHistory()

    val result = useCase.execute(command)

    result shouldBeEqual AdvanceOrderStatus.Result.Failure.InvalidTransition
    verify(exactly = 0) { eventStore.append(any(), any(), any()) }
    verify(exactly = 0) { notificationPublisher.publishStatusChanged(any()) }
  }

  @Test
  fun `execute should append OrderStatusAdvanced and then notify when transition is valid`() {
    val history = createSampleOrder(OrderStatus.PENDING).asHistory()
    every { eventStore.load(command.orderId) } returns history
    every { eventStore.append(any(), any(), any()) } just Runs

    val result = useCase.execute(command)

    result shouldBeEqual AdvanceOrderStatus.Result.Success
    verifyOrder {
      eventStore.append(
        command.orderId,
        history.size.toLong(),
        withArg { events ->
          val event = events.single()
          event.shouldBeInstanceOf<OrderStatusAdvanced>()
          event.previousStatus shouldBeEqual OrderStatus.PENDING
          event.newStatus shouldBeEqual OrderStatus.PREPARING
        },
      )
      notificationPublisher.publishStatusChanged(
        withArg { updatedOrder ->
          updatedOrder.status shouldBeEqual OrderStatus.PREPARING
          updatedOrder.id shouldBeEqual command.orderId
        },
      )
    }
  }
}
