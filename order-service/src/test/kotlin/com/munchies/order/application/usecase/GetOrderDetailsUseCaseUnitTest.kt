package com.munchies.order.application.usecase

import com.munchies.order.application.port.inbound.GetOrderDetails
import com.munchies.order.application.port.inbound.command.GetOrderDetailsCommand
import com.munchies.order.domain.model.CustomerId
import com.munchies.order.domain.model.OrderStatus
import com.munchies.order.domain.port.OrderViewRepository
import com.munchies.order.fixtures.asView
import com.munchies.order.fixtures.createSampleOrder
import com.munchies.order.fixtures.defaultCustomerId
import com.munchies.order.fixtures.defaultOrderId
import com.munchies.order.infrastructure.adapter.dto.factory.OrderDtoFactory.toDto
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

class GetOrderDetailsUseCaseUnitTest {

  private val views = mockk<OrderViewRepository>(relaxed = false)
  private val useCase = GetOrderDetailsUseCase(views)

  private val command = GetOrderDetailsCommand(defaultOrderId)

  @Test
  fun `execute should return OrderNotFound when the read model has no view for the order`() {
    every { views.findById(command.orderId) } returns null

    val result = useCase.execute(command)

    result shouldBeEqual GetOrderDetails.Result.Failure.OrderNotFound
  }

  @Test
  fun `execute should return Success with the DTO of the view when it exists`() {
    val order = createSampleOrder(OrderStatus.PENDING)
    every { views.findById(command.orderId) } returns order.asView()

    val result = useCase.execute(command)

    result.shouldBeInstanceOf<GetOrderDetails.Result.Success>()
    result.order shouldBeEqual order.toDto()
  }

  @Test
  fun `execute should return the order to the customer who owns it`() {
    val order = createSampleOrder(OrderStatus.PENDING)
    every { views.findById(defaultOrderId) } returns order.asView()

    val result = useCase.execute(GetOrderDetailsCommand(defaultOrderId, defaultCustomerId))

    result.shouldBeInstanceOf<GetOrderDetails.Result.Success>()
  }

  @Test
  fun `execute should answer OrderNotFound for another customer's order`() {
    every { views.findById(defaultOrderId) } returns createSampleOrder(OrderStatus.PENDING).asView()

    val result = useCase.execute(GetOrderDetailsCommand(defaultOrderId, CustomerId("someone-else")))

    result shouldBeEqual GetOrderDetails.Result.Failure.OrderNotFound
  }
}
