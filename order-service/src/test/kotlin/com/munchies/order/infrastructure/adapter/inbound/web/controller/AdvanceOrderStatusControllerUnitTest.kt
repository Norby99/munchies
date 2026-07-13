package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.order.application.port.inbound.AdvanceOrderStatus
import com.munchies.order.fixtures.createAdvanceOrderStatusRequest
import com.munchies.order.fixtures.defaultOrderId
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import io.mockk.every
import org.junit.jupiter.api.Test

class AdvanceOrderStatusControllerUnitTest : BaseOrderController() {

  @Test
  fun `returns 200 OK with confirmation message on success`() {
    val request = createAdvanceOrderStatusRequest(defaultOrderId)
    every { advanceOrderStatus.execute(any()) } returns AdvanceOrderStatus.Result.Success

    val response = controller.advanceOrderStatus(request)

    response.status shouldBe HttpStatus.OK
    response.body() shouldBe "Order status advanced"
  }

  @Test
  fun `returns 404 Not Found when order does not exist`() {
    val request = createAdvanceOrderStatusRequest(defaultOrderId)
    every {
      advanceOrderStatus.execute(
        any(),
      )
    } returns AdvanceOrderStatus.Result.Failure.OrderNotFound

    val response = controller.advanceOrderStatus(request)

    response.status shouldBe HttpStatus.NOT_FOUND
  }

  @Test
  fun `returns 400 Bad Request on invalid status transition`() {
    val request = createAdvanceOrderStatusRequest(defaultOrderId)
    every {
      advanceOrderStatus.execute(any())
    } returns AdvanceOrderStatus.Result.Failure.InvalidTransition

    val response = controller.advanceOrderStatus(request)

    response.status shouldBe HttpStatus.BAD_REQUEST
    response.body() shouldBe "Invalid status transition"
  }
}
