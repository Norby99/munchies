package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.commons.domain.port.ValidationException
import com.munchies.order.application.port.inbound.DiscardOrder
import com.munchies.order.fixtures.defaultOrderId
import com.munchies.order.infrastructure.adapter.inbound.web.controller.exception.NotFoundException
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import io.mockk.every
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class DiscardOrderControllerUnitTest : BaseOrderController() {

  @Test
  fun `return 200 OK on success`() {
    val id = defaultOrderId.value

    every { discardOrder.execute(any()) } returns DiscardOrder.Result.Success

    val response = controller.discardOrder(id)

    response.status shouldBe HttpStatus.OK
    response.body().code shouldBe HttpStatus.OK.code
    response.body().result shouldBe "Order discarded successfully"
  }

  @Test
  fun `throws NotFoundException on OrderNotFound`() {
    val id = defaultOrderId.value

    every { discardOrder.execute(any()) } returns
      DiscardOrder.Result.Failure.OrderNotFound

    val exception = assertThrows<NotFoundException> {
      controller.discardOrder(id)
    }
    exception.message shouldBe "Order not found"
  }

  @Test
  fun `throws ValidationException on OrderNotCancellable`() {
    val id = defaultOrderId.value

    every { discardOrder.execute(any()) } returns DiscardOrder.Result.Failure.OrderNotCancellable

    val exception = assertThrows<ValidationException> {
      controller.discardOrder(id)
    }
    exception.message shouldBe "Order cannot be cancelled"
  }
}
