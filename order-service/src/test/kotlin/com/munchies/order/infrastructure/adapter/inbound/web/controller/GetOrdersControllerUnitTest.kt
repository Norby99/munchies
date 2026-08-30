package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.order.application.port.inbound.GetOrders
import com.munchies.order.fixtures.createDeliveryOrder
import com.munchies.order.fixtures.createTakeawayOrder
import com.munchies.order.fixtures.defaultRestaurantId
import com.munchies.order.infrastructure.adapter.dto.factory.OrderDtoFactory.toDto
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import io.mockk.every
import org.junit.jupiter.api.Test

class GetOrdersControllerUnitTest : BaseOrderController() {

  @Test
  fun `returns 200 OK and DTOs when found`() {
    val realDto = listOf(createDeliveryOrder().toDto(), createTakeawayOrder().toDto())

    every { getOrders.execute(any()) } returns
      GetOrders.Result.Success(realDto)

    val response = controller.getOrders(
      restaurantId = defaultRestaurantId.value,
      customerId = null,
      orderStatus = null,
    )

    response.status shouldBe HttpStatus.OK
    response.body().code shouldBe HttpStatus.OK.code
    response.body().result shouldBe realDto
  }
}
