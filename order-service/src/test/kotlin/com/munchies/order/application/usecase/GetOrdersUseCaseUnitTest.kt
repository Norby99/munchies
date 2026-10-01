package com.munchies.order.application.usecase

import com.munchies.order.application.port.inbound.GetOrders
import com.munchies.order.application.port.inbound.command.GetOrdersCommand
import com.munchies.order.domain.model.CustomerId
import com.munchies.order.domain.model.OrderStatus
import com.munchies.order.domain.model.RestaurantId
import com.munchies.order.domain.port.OrderViewRepository
import com.munchies.order.fixtures.asView
import com.munchies.order.fixtures.createSampleOrder
import com.munchies.order.infrastructure.adapter.dto.factory.OrderDtoFactory.toDto
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class GetOrdersUseCaseUnitTest {

  private val resToFind = RestaurantId("dominos")
  private val customerToFind = CustomerId("alberto-01")
  private val statusToFind = OrderStatus.PENDING

  private val views = mockk<OrderViewRepository>(relaxed = false)
  private val useCase = GetOrdersUseCase(views)

  private val command = GetOrdersCommand(resToFind, customerToFind, statusToFind)

  @Test
  fun `execute should return OrderNotFound when no view matches the filters`() {
    every { views.findBy(resToFind, customerToFind, statusToFind) } returns emptyList()

    val result = useCase.execute(command)

    result.shouldBeInstanceOf<GetOrders.Result.Failure.OrderNotFound>()
  }

  @Test
  fun `execute should delegate filtering to the read model and map the views to DTOs`() {
    val orders = listOf(
      createSampleOrder(OrderStatus.PENDING).copy(
        restaurantId = resToFind,
        customerId = customerToFind,
      ),
    )
    every { views.findBy(resToFind, customerToFind, statusToFind) } returns
      orders.map { it.asView() }

    val result = useCase.execute(command)

    result.shouldBeInstanceOf<GetOrders.Result.Success>()
    result.orders shouldBeEqual orders.map { it.toDto() }
    verify(exactly = 1) { views.findBy(resToFind, customerToFind, statusToFind) }
  }
}
