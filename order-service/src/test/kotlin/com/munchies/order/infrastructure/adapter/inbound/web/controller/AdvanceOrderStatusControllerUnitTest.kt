package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.commons.domain.port.ValidationException
import com.munchies.order.application.port.inbound.AdvanceOrderStatus
import com.munchies.order.fixtures.createAdvanceOrderStatusRequest
import com.munchies.order.fixtures.defaultOrderId
import com.munchies.order.infrastructure.adapter.inbound.web.controller.exception.NotFoundException
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import io.mockk.every
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AdvanceOrderStatusControllerUnitTest : BaseOrderController() {

  @Test
  fun `returns 200 OK with confirmation message on success`() {
    val request = createAdvanceOrderStatusRequest(defaultOrderId)
    every { advanceOrderStatus.execute(any()) } returns AdvanceOrderStatus.Result.Success

    val response = controller.advanceOrderStatus(request)

    response.status shouldBe HttpStatus.OK
    response.body().code shouldBe HttpStatus.OK.code
    response.body().result shouldBe "Order status advanced successfully"
  }

  @Test
  fun `throws NotFoundException when order does not exist`() {
    val request = createAdvanceOrderStatusRequest(defaultOrderId)
    every {
      advanceOrderStatus.execute(any())
    } returns AdvanceOrderStatus.Result.Failure.OrderNotFound

    val exception = assertThrows<NotFoundException> {
      controller.advanceOrderStatus(request)
    }
    exception.message shouldBe "Order not found"
  }

  @Test
  fun `throws ValidationException on invalid status transition`() {
    val request = createAdvanceOrderStatusRequest(defaultOrderId)
    every {
      advanceOrderStatus.execute(any())
    } returns AdvanceOrderStatus.Result.Failure.InvalidTransition

    val exception = assertThrows<ValidationException> {
      controller.advanceOrderStatus(request)
    }
    exception.message shouldBe "Invalid status transition"
  }
}
