package com.munchies.order.application.usecase

import com.munchies.order.application.port.inbound.GetOrders
import com.munchies.order.application.port.inbound.command.GetOrdersCommand
import com.munchies.order.domain.model.CustomerId
import com.munchies.order.domain.model.OrderStatus
import com.munchies.order.domain.model.RestaurantId
import com.munchies.order.domain.ports.OrderRepository
import com.munchies.order.fixtures.createSampleOrder
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

class GetOrdersUseCaseUnitTest {

  private val resToFind = RestaurantId("dominos")
  private val customerToFind = CustomerId("alberto-01")
  private val statusToFind = OrderStatus.PENDING

  private val repository = mockk<OrderRepository>(relaxed = false)
  private val useCase = GetOrdersUseCase(repository)

  private val mockOrderDto = mockk<OrderDto>()

  private val command = GetOrdersCommand(resToFind, customerToFind, statusToFind)

  @BeforeEach
  fun setUp() {
    mockkObject(OrderDtoFactory)
  }

  @AfterEach
  fun tearDown() {
    unmockkObject(OrderDtoFactory)
  }

  @Test
  fun `execute should return an empty list when repository cannot find orders`() {
    every { repository.findAll() } returns emptyList()

    val result = useCase.execute(command)

    result.shouldBeInstanceOf<GetOrders.Result.Success>()
    result.orders shouldBeEqual emptyList<OrderDto>()
  }

  @Test
  fun `execute should return Success with mapped DTO when order exists`() {
    val ordersToFind = listOf(
      createSampleOrder(OrderStatus.PENDING).copy(
        restaurantId = resToFind,
        customerId = customerToFind,
      ),
      createSampleOrder(OrderStatus.PENDING).copy(
        restaurantId = resToFind,
        customerId = customerToFind,
      ),
    )

    val existingOrders = listOf(
      createSampleOrder(OrderStatus.PENDING),
      createSampleOrder(OrderStatus.COMPLETED),
      createSampleOrder(OrderStatus.ON_THE_WAY).copy(restaurantId = resToFind),
      createSampleOrder(OrderStatus.COMPLETED).copy(restaurantId = resToFind),
    ) + ordersToFind

    every { repository.findAll() } returns existingOrders

    val result = useCase.execute(command)

    result.shouldBeInstanceOf<GetOrders.Result.Success>()
    result.orders shouldBeEqual ordersToFind.map { o -> o.toDto() }
  }
}
