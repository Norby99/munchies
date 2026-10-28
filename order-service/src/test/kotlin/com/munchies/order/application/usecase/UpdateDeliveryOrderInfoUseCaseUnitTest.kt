package com.munchies.order.application.usecase

import com.munchies.order.application.port.inbound.UpdateDeliveryOrderInfo
import com.munchies.order.domain.model.CustomerId
import com.munchies.order.domain.model.OrderStatus
import com.munchies.order.domain.model.event.DeliveryInfoUpdated
import com.munchies.order.domain.port.OrderEventStore
import com.munchies.order.fixtures.asHistory
import com.munchies.order.fixtures.createDeliveryOrder
import com.munchies.order.fixtures.createSampleOrder
import com.munchies.order.fixtures.createUpdateDeliveryOrderInfoCommand
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

class UpdateDeliveryOrderInfoUseCaseUnitTest {

  private val eventStore = mockk<OrderEventStore>(relaxed = false)
  private val useCase = UpdateDeliveryOrderInfoUseCase(eventStore)

  @Test
  fun `execute should return OrderNotFound when order does not exist`() {
    val command = createUpdateDeliveryOrderInfoCommand()
    every { eventStore.load(command.orderId) } returns emptyList()

    val result = useCase.execute(command)

    result shouldBeEqual UpdateDeliveryOrderInfo.Result.Failure.OrderNotFound
    verify(exactly = 0) { eventStore.append(any(), any(), any()) }
  }

  @Test
  fun `execute should return Unauthorized when order belongs to another customer`() {
    val command = createUpdateDeliveryOrderInfoCommand()
    every { eventStore.load(command.orderId) } returns createDeliveryOrder()
      .copy(customerId = CustomerId("wrong-customer-id"))
      .asHistory()

    val result = useCase.execute(command)

    result shouldBeEqual UpdateDeliveryOrderInfo.Result.Failure.Unauthorized
    verify(exactly = 0) { eventStore.append(any(), any(), any()) }
  }

  @Test
  fun `execute should return OrderNotFound when order exists but is NOT a DeliveryOrder`() {
    val command = createUpdateDeliveryOrderInfoCommand()
    every { eventStore.load(command.orderId) } returns
      createSampleOrder(OrderStatus.PENDING).asHistory()

    val result = useCase.execute(command)

    result shouldBeEqual UpdateDeliveryOrderInfo.Result.Failure.OrderNotFound
    verify(exactly = 0) { eventStore.append(any(), any(), any()) }
  }

  @Test
  fun `execute should return InvalidDate when domain logic rejects the estimated time`() {
    val command = createUpdateDeliveryOrderInfoCommand(estimatedDeliveryTime = pastTime)
    every { eventStore.load(command.orderId) } returns createDeliveryOrder().asHistory()

    val result = useCase.execute(command)

    result shouldBeEqual UpdateDeliveryOrderInfo.Result.Failure.InvalidDate
    verify(exactly = 0) { eventStore.append(any(), any(), any()) }
  }

  @Test
  fun `execute should append DeliveryInfoUpdated and return Success when command is valid`() {
    val command = createUpdateDeliveryOrderInfoCommand(estimatedDeliveryTime = futureTime)
    val history = createDeliveryOrder().asHistory()
    every { eventStore.load(command.orderId) } returns history
    every { eventStore.append(any(), any(), any()) } just Runs

    val result = useCase.execute(command)

    result shouldBeEqual UpdateDeliveryOrderInfo.Result.Success
    verify(exactly = 1) {
      eventStore.append(
        command.orderId,
        history.size.toLong(),
        withArg { events ->
          val event = events.single()
          event.shouldBeInstanceOf<DeliveryInfoUpdated>()
          event.deliveryInfo.deliveryAddress shouldBeEqual command.deliveryAddress
          event.deliveryInfo.bellName shouldBeEqual command.bellName
          event.deliveryInfo.customerPhone shouldBeEqual command.customerPhone
          event.deliveryInfo.estimatedDeliveryTime shouldBeEqual command.estimatedDeliveryTime
        },
      )
    }
  }
}
