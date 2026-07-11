package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.order.application.port.inbound.GetOrderDetails
import com.munchies.order.fixtures.createSampleOrder
import com.munchies.order.fixtures.defaultOrderId
import com.munchies.order.infrastructure.adapter.dto.factory.OrderDtoFactory.toDto
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import io.mockk.every
import org.junit.jupiter.api.Test

class GetOrderDetailsControllerUnitTest : BaseOrderController() {

  @Test
  fun `returns 200 OK and DTO when found`() {
    val realDto = createSampleOrder().toDto()
    every { getOrderDetails.execute(any()) } returns GetOrderDetails.Result.Success(realDto)

    val response = controller.getOrderDetails(realDto.orderId)

    response.status shouldBe HttpStatus.OK
    response.body() shouldBeEqual realDto
  }

  @Test
  fun `returns 404 Not Found when use case returns OrderNotFound`() {
    every { getOrderDetails.execute(any()) } returns GetOrderDetails.Result.Failure.OrderNotFound

    val response = controller.getOrderDetails(defaultOrderId.value)

    response.status shouldBe HttpStatus.NOT_FOUND
  }
}
