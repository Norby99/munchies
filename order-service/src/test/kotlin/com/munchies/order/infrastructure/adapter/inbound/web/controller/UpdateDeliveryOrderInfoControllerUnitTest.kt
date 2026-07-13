package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.order.application.port.inbound.UpdateDeliveryOrderInfo
import com.munchies.order.fixtures.createUpdateDeliveryOrderRequest
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import io.mockk.every
import org.junit.jupiter.api.Test

class UpdateDeliveryOrderInfoControllerUnitTest : BaseOrderController() {

  @Test
  fun `returns 200 OK on success`() {
    val request = createUpdateDeliveryOrderRequest()

    every { updateDeliveryOrderInfo.execute(any()) } returns UpdateDeliveryOrderInfo.Result.Success

    val response = controller.updateDeliveryOrderInfo(request)

    response.status shouldBe HttpStatus.OK
  }

  @Test
  fun `returns 404 Not Found on OrderNotFound`() {
    val request = createUpdateDeliveryOrderRequest()

    every {
      updateDeliveryOrderInfo.execute(any())
    } returns UpdateDeliveryOrderInfo.Result.Failure.OrderNotFound

    val response = controller.updateDeliveryOrderInfo(request)

    response.status shouldBe HttpStatus.NOT_FOUND
  }

  @Test
  fun `returns 400 Bad Request on Unauthorized`() {
    val request = createUpdateDeliveryOrderRequest()

    every {
      updateDeliveryOrderInfo.execute(any())
    } returns UpdateDeliveryOrderInfo.Result.Failure.Unauthorized

    val response = controller.updateDeliveryOrderInfo(request)

    response.status shouldBe HttpStatus.BAD_REQUEST
  }

  @Test
  fun `returns 400 Bad Request on InvalidDate`() {
    val request = createUpdateDeliveryOrderRequest()

    every {
      updateDeliveryOrderInfo.execute(any())
    } returns UpdateDeliveryOrderInfo.Result.Failure.InvalidDate

    val response = controller.updateDeliveryOrderInfo(request)

    response.status shouldBe HttpStatus.BAD_REQUEST
  }
}
