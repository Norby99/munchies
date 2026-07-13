package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.order.application.port.inbound.UpdateTakeawayOrderInfo
import com.munchies.order.fixtures.createUpdateTakeawayOrderRequest
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import io.mockk.every
import org.junit.jupiter.api.Test

class UpdateTakeawayOrderInfoControllerUnitTest : BaseOrderController() {
  @Test
  fun `returns 200 OK on success`() {
    val request = createUpdateTakeawayOrderRequest()

    every {
      updateTakeawayOrderInfo.execute(any())
    } returns UpdateTakeawayOrderInfo.Result.Success

    val response = controller.updateTakeawayOrderInfo(request)

    response.status shouldBe HttpStatus.OK
  }

  @Test
  fun `returns 404 Not Found on OrderNotFound`() {
    val request = createUpdateTakeawayOrderRequest()

    every {
      updateTakeawayOrderInfo.execute(any())
    } returns UpdateTakeawayOrderInfo.Result.Failure.OrderNotFound

    val response = controller.updateTakeawayOrderInfo(request)

    response.status shouldBe HttpStatus.NOT_FOUND
  }

  @Test
  fun `returns 400 Bad Request on Unauthorized`() {
    val request = createUpdateTakeawayOrderRequest()

    every {
      updateTakeawayOrderInfo.execute(any())
    } returns UpdateTakeawayOrderInfo.Result.Failure.Unauthorized

    val response = controller.updateTakeawayOrderInfo(request)

    response.status shouldBe HttpStatus.BAD_REQUEST
  }

  @Test
  fun `returns 400 Bad Request on InvalidDate`() {
    val request = createUpdateTakeawayOrderRequest()

    every {
      updateTakeawayOrderInfo.execute(any())
    } returns UpdateTakeawayOrderInfo.Result.Failure.InvalidDate

    val response = controller.updateTakeawayOrderInfo(request)

    response.status shouldBe HttpStatus.BAD_REQUEST
  }
}
