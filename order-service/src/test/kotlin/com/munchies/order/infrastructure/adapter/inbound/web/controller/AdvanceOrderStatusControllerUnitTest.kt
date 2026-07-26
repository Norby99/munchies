package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.order.application.port.inbound.AdvanceOrderStatus
import com.munchies.order.fixtures.createAdvanceOrderStatusRequest
import com.munchies.order.fixtures.defaultOrderId
import com.munchies.order.infrastructure.adapter.outbound.response.AdvanceOrderStatusResponse
import com.munchies.order.infrastructure.adapter.outbound.response.AdvanceOrderStatusResponseType
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
    response.body().code shouldBe HttpStatus.OK.code
    response.body().type shouldBe AdvanceOrderStatusResponseType.SUCCESS
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
    response.bd<AdvanceOrderStatusResponse>().code shouldBe HttpStatus.NOT_FOUND.code
    response.bd<AdvanceOrderStatusResponse>().type shouldBe
      AdvanceOrderStatusResponseType.ORDER_NOT_FOUND
  }

  @Test
  fun `returns 400 Bad Request on invalid status transition`() {
    val request = createAdvanceOrderStatusRequest(defaultOrderId)
    every {
      advanceOrderStatus.execute(any())
    } returns AdvanceOrderStatus.Result.Failure.InvalidTransition

    val response = controller.advanceOrderStatus(request)

    response.status shouldBe HttpStatus.BAD_REQUEST
    response.bd<AdvanceOrderStatusResponse>().code shouldBe
      HttpStatus.BAD_REQUEST.code
    response.bd<AdvanceOrderStatusResponse>().type shouldBe
      AdvanceOrderStatusResponseType.INVALID_TRANSACTION
  }
}
