package com.munchies.order.application.usecase

import com.munchies.order.application.port.inbound.UpdateTakeawayOrderInfo
import com.munchies.order.domain.model.CustomerId
import com.munchies.order.domain.model.OrderStatus
import com.munchies.order.domain.model.event.TakeawayInfoUpdated
import com.munchies.order.domain.port.OrderEventStore
import com.munchies.order.fixtures.asHistory
import com.munchies.order.fixtures.createDeliveryOrder
import com.munchies.order.fixtures.createSampleOrder
import com.munchies.order.fixtures.createUpdateTakeawayOrderInfoUseCommand
import com.munchies.order.fixtures.futureTime
import com.munchies.order.fixtures.pastTime
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class UpdateTakeawayOrderInfoUseCaseUnitTest {

  private val eventStore = mockk<OrderEventStore>(relaxed = false)
  private val useCase = UpdateTakeawayOrderInfoUseCase(eventStore)

  @Test
  fun `execute should return OrderNotFound when order does not exist`() {
    val command = createUpdateTakeawayOrderInfoUseCommand()
    every { eventStore.load(command.orderId) } returns emptyList()

    val result = useCase.execute(command)

    result shouldBeEqual UpdateTakeawayOrderInfo.Result.Failure.OrderNotFound
    verify(exactly = 0) { eventStore.append(any(), any(), any()) }
  }

  @Test
  fun `execute should return Unauthorized when order belongs to a different customer`() {
    val command = createUpdateTakeawayOrderInfoUseCommand()
    every { eventStore.load(command.orderId) } returns createSampleOrder(OrderStatus.PENDING)
      .copy(customerId = CustomerId("another-customer-999"))
      .asHistory()

    val result = useCase.execute(command)

    result shouldBeEqual UpdateTakeawayOrderInfo.Result.Failure.Unauthorized
    verify(exactly = 0) { eventStore.append(any(), any(), any()) }
  }

  @Test
  fun `execute should return OrderNotFound when order exists but is NOT a TakeawayOrder`() {
    val command = createUpdateTakeawayOrderInfoUseCommand()
    every { eventStore.load(command.orderId) } returns createDeliveryOrder().asHistory()

    val result = useCase.execute(command)

    result shouldBeEqual UpdateTakeawayOrderInfo.Result.Failure.OrderNotFound
    verify(exactly = 0) { eventStore.append(any(), any(), any()) }
  }

  @Test
  fun `execute should return InvalidDate when domain logic rejects the pickup time`() {
    val command = createUpdateTakeawayOrderInfoUseCommand(pickupTime = pastTime)
    every { eventStore.load(command.orderId) } returns
      createSampleOrder(OrderStatus.PENDING).asHistory()

    val result = useCase.execute(command)

    result shouldBeEqual UpdateTakeawayOrderInfo.Result.Failure.InvalidDate
    verify(exactly = 0) { eventStore.append(any(), any(), any()) }
  }

  @Test
  fun `execute should append TakeawayInfoUpdated and return Success when command is valid`() {
    val command = createUpdateTakeawayOrderInfoUseCommand(pickupTime = futureTime)
    val history = createSampleOrder(OrderStatus.PENDING).asHistory()
    every { eventStore.load(command.orderId) } returns history
    every { eventStore.append(any(), any(), any()) } just Runs

    val result = useCase.execute(command)

    result shouldBeEqual UpdateTakeawayOrderInfo.Result.Success
    verify(exactly = 1) {
      eventStore.append(
        command.orderId,
        history.size.toLong(),
        withArg { events ->
          val event = events.single()
          event.shouldBeInstanceOf<TakeawayInfoUpdated>()
          event.takeawayInfo.customerName shouldBeEqual command.customerName
          event.takeawayInfo.pickupTime shouldBeEqual command.pickupTime
        },
      )
    }
  }
}
