package com.munchies.order.application.usecase

import com.munchies.order.application.port.inbound.GetOrderDetails
import com.munchies.order.application.port.inbound.command.GetOrderDetailsCommand
import com.munchies.order.domain.model.OrderStatus
import com.munchies.order.domain.ports.OrderRepository
import com.munchies.order.fixtures.createSampleOrder
import com.munchies.order.fixtures.defaultOrderId
import com.munchies.order.infrastructure.adapter.dto.OrderDto
import com.munchies.order.infrastructure.adapter.dto.factory.OrderDtoFactory
import com.munchies.order.infrastructure.adapter.dto.factory.OrderDtoFactory.toDto
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetOrderDetailsUseCaseTest {

  private val repository = mockk<OrderRepository>(relaxed = false)
  private val useCase = GetOrderDetailsUseCase(repository)

  private val mockOrderDto = mockk<OrderDto>()

  private val command = GetOrderDetailsCommand(defaultOrderId)

  @BeforeEach
  fun setUp() {
    mockkObject(OrderDtoFactory)
  }

  @AfterEach
  fun tearDown() {
    unmockkObject(OrderDtoFactory)
  }

  @Test
  fun `execute should return OrderNotFound when repository cannot find the order`() {
    every { repository.findById(command.orderId) } returns null

    val result = useCase.execute(command)

    result shouldBeEqual GetOrderDetails.Result.Failure.OrderNotFound
  }

  @Test
  fun `execute should return Success with mapped DTO when order exists`() {
    val existingOrder = createSampleOrder(OrderStatus.PENDING)

    every { repository.findById(command.orderId) } returns existingOrder
    every { existingOrder.toDto() } returns mockOrderDto

    val result = useCase.execute(command)

    result.shouldBeInstanceOf<GetOrderDetails.Result.Success>()
    result.order shouldBeEqual mockOrderDto
  }
}
