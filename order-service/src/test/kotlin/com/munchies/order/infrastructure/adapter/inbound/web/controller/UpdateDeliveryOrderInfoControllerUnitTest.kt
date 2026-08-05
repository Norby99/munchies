package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.commons.domain.port.ValidationException
import com.munchies.order.application.port.inbound.UpdateDeliveryOrderInfo
import com.munchies.order.fixtures.createUpdateDeliveryOrderRequest
import com.munchies.order.infrastructure.adapter.inbound.web.controller.exception.NotFoundException
import com.munchies.order.infrastructure.adapter.inbound.web.controller.exception.UnauthorizedException
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import io.mockk.every
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UpdateDeliveryOrderInfoControllerUnitTest : BaseOrderController() {

  @Test
  fun `returns 200 OK on success`() {
    val request = createUpdateDeliveryOrderRequest()

    every { updateDeliveryOrderInfo.execute(any()) } returns
      UpdateDeliveryOrderInfo.Result.Success

    val response = controller.updateDeliveryOrderInfo(request)

    response.status shouldBe HttpStatus.OK
    response.body().code shouldBe HttpStatus.OK.code
    response.body().result shouldBe "Delivery info updated successfully"
  }

  @Test
  fun `throws NotFoundException on OrderNotFound`() {
    val request = createUpdateDeliveryOrderRequest()

    every {
      updateDeliveryOrderInfo.execute(any())
    } returns UpdateDeliveryOrderInfo.Result.Failure.OrderNotFound

    val exception = assertThrows<NotFoundException> {
      controller.updateDeliveryOrderInfo(request)
    }
    exception.message shouldBe "Order not found"
  }

  @Test
  fun `throws UnauthorizedException on Unauthorized`() {
    val request = createUpdateDeliveryOrderRequest()

    every {
      updateDeliveryOrderInfo.execute(any())
    } returns UpdateDeliveryOrderInfo.Result.Failure.Unauthorized

    val exception = assertThrows<UnauthorizedException> {
      controller.updateDeliveryOrderInfo(request)
    }
    exception.message shouldBe "Unauthorized"
  }

  @Test
  fun `throws ValidationException on InvalidDate`() {
    val request = createUpdateDeliveryOrderRequest()

    every {
      updateDeliveryOrderInfo.execute(any())
    } returns UpdateDeliveryOrderInfo.Result.Failure.InvalidDate

    val exception = assertThrows<ValidationException> {
      controller.updateDeliveryOrderInfo(request)
    }
    exception.message shouldBe "Invalid date"
  }
}
