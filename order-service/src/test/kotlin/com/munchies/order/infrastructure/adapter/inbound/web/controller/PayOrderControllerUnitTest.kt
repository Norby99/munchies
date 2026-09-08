package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.commons.domain.port.ValidationException
import com.munchies.order.application.port.inbound.PayOrder
import com.munchies.order.fixtures.defaultOrderId
import com.munchies.order.infrastructure.adapter.inbound.web.controller.exception.NotFoundException
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import io.mockk.every
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PayOrderControllerUnitTest : BaseOrderController() {

  @Test
  fun `returns 200 OK with confirmation message on success`() {
    every { payOrder.execute(any()) } returns PayOrder.Result.Success

    val response = controller.payOrder(defaultOrderId.toString())

    response.status shouldBe HttpStatus.OK
    response.body().code shouldBe HttpStatus.OK.code
    response.body().result shouldBe "Payment successful"
  }

  @Test
  fun `throws NotFoundException when order does not exist`() {
    every { payOrder.execute(any()) } returns PayOrder.Result.Failure.OrderNotFound

    val exception = assertThrows<NotFoundException> {
      controller.payOrder(defaultOrderId.toString())
    }
    exception.message shouldBe "Order not found"
  }

  @Test
  fun `throws ValidationException on invalid payment attempt`() {
    every { payOrder.execute(any()) } returns PayOrder.Result.Failure.AlreadyPaid

    val exception = assertThrows<ValidationException> {
      controller.payOrder(defaultOrderId.toString())
    }
    exception.message shouldBe "Order already paid"
  }
}
