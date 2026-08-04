package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.commons.domain.port.ValidationException
import com.munchies.order.application.port.inbound.UpdateTakeawayOrderInfo
import com.munchies.order.fixtures.createUpdateTakeawayOrderRequest
import com.munchies.order.infrastructure.adapter.inbound.web.controller.exception.NotFoundException
import com.munchies.order.infrastructure.adapter.inbound.web.controller.exception.UnauthorizedException
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import io.mockk.every
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UpdateTakeawayOrderInfoControllerUnitTest : BaseOrderController() {
  @Test
  fun `returns 200 OK on success`() {
    val request = createUpdateTakeawayOrderRequest()

    every {
      updateTakeawayOrderInfo.execute(any())
    } returns UpdateTakeawayOrderInfo.Result.Success

    val response = controller.updateTakeawayOrderInfo(request)

    response.status shouldBe HttpStatus.OK
    response.body().code shouldBe HttpStatus.OK.code
    response.body().result shouldBe "Takeaway info updated successfully"
  }

  @Test
  fun `throws NotFoundException on OrderNotFound`() {
    val request = createUpdateTakeawayOrderRequest()

    every {
      updateTakeawayOrderInfo.execute(any())
    } returns UpdateTakeawayOrderInfo.Result.Failure.OrderNotFound

    val exception = assertThrows<NotFoundException> {
      controller.updateTakeawayOrderInfo(request)
    }
    exception.message shouldBe "Order not found"
  }

  @Test
  fun `throws UnauthorizedException on Unauthorized`() {
    val request = createUpdateTakeawayOrderRequest()

    every {
      updateTakeawayOrderInfo.execute(any())
    } returns UpdateTakeawayOrderInfo.Result.Failure.Unauthorized

    val exception = assertThrows<UnauthorizedException> {
      controller.updateTakeawayOrderInfo(request)
    }
    exception.message shouldBe "Unauthorized"
  }

  @Test
  fun `throws ValidationException on InvalidDate`() {
    val request = createUpdateTakeawayOrderRequest()

    every {
      updateTakeawayOrderInfo.execute(any())
    } returns UpdateTakeawayOrderInfo.Result.Failure.InvalidDate

    val exception = assertThrows<ValidationException> {
      controller.updateTakeawayOrderInfo(request)
    }
    exception.message shouldBe "Invalid date"
  }
}
