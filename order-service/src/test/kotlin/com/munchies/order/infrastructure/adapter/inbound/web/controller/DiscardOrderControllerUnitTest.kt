package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.commons.domain.port.ValidationException
import com.munchies.order.application.port.inbound.DiscardOrder
import com.munchies.order.application.port.inbound.command.DiscardOrderCommand
import com.munchies.order.fixtures.defaultCustomerId
import com.munchies.order.fixtures.defaultOrderId
import com.munchies.order.infrastructure.adapter.inbound.web.controller.exception.NotFoundException
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class DiscardOrderControllerUnitTest : BaseOrderController() {

  @Test
  fun `return 200 OK on success`() {
    val id = defaultOrderId.value

    every { discardOrder.execute(any()) } returns DiscardOrder.Result.Success

    val response = controller.discardOrder(id, null)

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
      controller.discardOrder(id, null)
    }
    exception.message shouldBe "Order not found"
  }

  @Test
  fun `throws ValidationException on OrderNotCancellable`() {
    val id = defaultOrderId.value

    every { discardOrder.execute(any()) } returns DiscardOrder.Result.Failure.OrderNotCancellable

    val exception = assertThrows<ValidationException> {
      controller.discardOrder(id, null)
    }
    exception.message shouldBe "Order cannot be cancelled"
  }

  @Test
  fun `throws ValidationException on OrderAlreadyPaid`() {
    every { discardOrder.execute(any()) } returns DiscardOrder.Result.Failure.OrderAlreadyPaid

    val exception = assertThrows<ValidationException> {
      controller.discardOrder(defaultOrderId.value, defaultCustomerId.value)
    }
    exception.message shouldBe "A paid order cannot be cancelled"
  }

  @Test
  fun `passes the caller from the query to the use case`() {
    every { discardOrder.execute(any()) } returns DiscardOrder.Result.Success

    controller.discardOrder(defaultOrderId.value, defaultCustomerId.value)

    verify {
      discardOrder.execute(DiscardOrderCommand(defaultOrderId, defaultCustomerId))
    }
  }
}
