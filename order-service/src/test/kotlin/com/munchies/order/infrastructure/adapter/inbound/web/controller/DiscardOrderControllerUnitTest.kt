package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.order.application.port.inbound.DiscardOrder
import com.munchies.order.fixtures.defaultOrderId
import com.munchies.order.infrastructure.adapter.outbound.response.DiscardOrderResponse
import com.munchies.order.infrastructure.adapter.outbound.response.DiscardOrderResponseType
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import io.mockk.every
import org.junit.jupiter.api.Test

class DiscardOrderControllerUnitTest : BaseOrderController() {

  @Test
  fun `return 200 OK on success`() {
    val id = defaultOrderId.value

    every { discardOrder.execute(any()) } returns DiscardOrder.Result.Success

    val response = controller.discardOrder(id)

    response.status shouldBe HttpStatus.OK
    response.body().code shouldBe HttpStatus.OK.code
    response.body().type shouldBe DiscardOrderResponseType.SUCCESS
  }

  @Test
  fun `returns 404 Not Found on OrderNotFound`() {
    val id = defaultOrderId.value

    every { discardOrder.execute(any()) } returns DiscardOrder.Result.Failure.OrderNotFound

    val response = controller.discardOrder(id)

    response.status shouldBe HttpStatus.NOT_FOUND
    response.bd<DiscardOrderResponse>().code shouldBe HttpStatus.NOT_FOUND.code
    response.bd<DiscardOrderResponse>().type shouldBe DiscardOrderResponseType.ORDER_NOT_FOUND
  }

  @Test
  fun `returns 400 Bad Request on OrderNotCancellable`() {
    val id = defaultOrderId.value

    every { discardOrder.execute(any()) } returns DiscardOrder.Result.Failure.OrderNotCancellable

    val response = controller.discardOrder(id)

    response.status shouldBe HttpStatus.BAD_REQUEST
    response.bd<DiscardOrderResponse>().code shouldBe HttpStatus.BAD_REQUEST.code
    response.bd<DiscardOrderResponse>().type shouldBe DiscardOrderResponseType.ORDER_NOT_CANCELLABLE
  }
}
