package com.munchies.order.application.usecase

import com.munchies.order.application.port.inbound.UpdateDeliveryOrderInfo
import com.munchies.order.domain.model.CustomerId
import com.munchies.order.domain.model.DeliveryOrder
import com.munchies.order.domain.model.OrderStatus
import com.munchies.order.domain.ports.OrderRepository
import com.munchies.order.fixtures.createDeliveryOrder
import com.munchies.order.fixtures.createSampleOrder
import com.munchies.order.fixtures.createUpdateDeliveryOrderInfoCommand
import com.munchies.order.fixtures.futureTime
import com.munchies.order.fixtures.pastTime
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class UpdateDeliveryOrderInfoUseCaseTest {

  private val repository = mockk<OrderRepository>(relaxed = false)
  private val useCase = UpdateDeliveryOrderInfoUseCase(repository)

  @Test
  fun `execute should return OrderNotFound when order does not exist`() {
    val command = createUpdateDeliveryOrderInfoCommand()
    every { repository.findById(command.orderId) } returns null

    val result = useCase.execute(command)

    result shouldBeEqual UpdateDeliveryOrderInfo.Result.Failure.OrderNotFound
    verify(exactly = 0) { repository.update(any()) }
  }

  @Test
  fun `execute should return Unauthorized when order belongs to another customer`() {
    val command = createUpdateDeliveryOrderInfoCommand()
    val orderOfAnotherCustomer = createDeliveryOrder()
      .copy(customerId = CustomerId("wrong-customer-id"))

    every { repository.findById(command.orderId) } returns orderOfAnotherCustomer

    val result = useCase.execute(command)

    result shouldBeEqual UpdateDeliveryOrderInfo.Result.Failure.Unauthorized
    verify(exactly = 0) { repository.update(any()) }
  }

  @Test
  fun `execute should return OrderNotFound when order exists but is NOT a DeliveryOrder`() {
    val command = createUpdateDeliveryOrderInfoCommand()
    val takeawayOrder = createSampleOrder(OrderStatus.PENDING)

    every { repository.findById(command.orderId) } returns takeawayOrder

    val result = useCase.execute(command)

    result shouldBeEqual UpdateDeliveryOrderInfo.Result.Failure.OrderNotFound
    verify(exactly = 0) { repository.update(any()) }
  }

  @Test
  fun `execute should return InvalidDate when domain logic rejects the estimated time`() {
    val command = createUpdateDeliveryOrderInfoCommand(estimatedDeliveryTime = pastTime)
    val deliveryOrder = createDeliveryOrder()

    every { repository.findById(command.orderId) } returns deliveryOrder

    val result = useCase.execute(command)

    result shouldBeEqual UpdateDeliveryOrderInfo.Result.Failure.InvalidDate
    verify(exactly = 0) { repository.update(any()) }
  }

  @Test
  fun `execute should update repository and return Success when command is valid`() {
    val command = createUpdateDeliveryOrderInfoCommand(estimatedDeliveryTime = futureTime)
    val deliveryOrder = createDeliveryOrder()

    every { repository.findById(command.orderId) } returns deliveryOrder
    every { repository.update(any()) } returns Unit

    val result = useCase.execute(command)

    result shouldBeEqual UpdateDeliveryOrderInfo.Result.Success
    verify(exactly = 1) {
      repository.update(
        withArg { updatedOrder ->
          updatedOrder.shouldBeInstanceOf<DeliveryOrder>()

          updatedOrder.deliveryInfo.deliveryAddress shouldBeEqual command.deliveryAddress
          updatedOrder.deliveryInfo.bellName shouldBeEqual command.bellName
          updatedOrder.deliveryInfo.customerPhone shouldBeEqual command.customerPhone
          updatedOrder.deliveryInfo.estimatedDeliveryTime shouldBeEqual
            command.estimatedDeliveryTime
        },
      )
    }
  }
}
