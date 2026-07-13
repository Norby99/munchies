package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.order.application.port.inbound.DiscardOrder
import com.munchies.order.fixtures.createDiscardOrderRequest
import com.munchies.order.fixtures.defaultOrderId
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import io.mockk.every
import org.junit.jupiter.api.Test

class DiscardOrderControllerUnitTest : BaseOrderController() {

  @Test
  fun `return 200 OK on success`() {
    val request = createDiscardOrderRequest(defaultOrderId)

    every { discardOrder.execute(any()) } returns DiscardOrder.Result.Success

    val response = controller.discardOrder(request)

    response.status shouldBe HttpStatus.OK
    response.body() shouldBeEqual "Order discarded"
  }

  @Test
  fun `returns 404 Not Found on OrderNotFound`() {
    val request = createDiscardOrderRequest(defaultOrderId)

    every { discardOrder.execute(any()) } returns DiscardOrder.Result.Failure.OrderNotFound

    val response = controller.discardOrder(request)

    response.status shouldBe HttpStatus.NOT_FOUND
  }

  @Test
  fun `returns 400 Bad Request on Unauthorized`() {
    val request = createDiscardOrderRequest(defaultOrderId)

    every { discardOrder.execute(any()) } returns DiscardOrder.Result.Failure.Unauthorized

    val response = controller.discardOrder(request)

    response.status shouldBe HttpStatus.BAD_REQUEST
  }

  @Test
  fun `returns 400 Bad Request on OrderNotCancellable`() {
    val request = createDiscardOrderRequest(defaultOrderId)

    every { discardOrder.execute(any()) } returns DiscardOrder.Result.Failure.OrderNotCancellable

    val response = controller.discardOrder(request)

    response.status shouldBe HttpStatus.BAD_REQUEST
  }
}
