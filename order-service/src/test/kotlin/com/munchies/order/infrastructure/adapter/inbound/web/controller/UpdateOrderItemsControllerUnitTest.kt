package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.order.application.port.inbound.UpdateOrderItems
import com.munchies.order.fixtures.createUpdateOrderItemsRequest
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import io.mockk.every
import org.junit.jupiter.api.Test

class UpdateOrderItemsControllerUnitTest : BaseOrderController() {

  @Test
  fun `returns 200 OK on success`() {
    val request = createUpdateOrderItemsRequest()

    every {
      updateOrderItems.execute(any())
    } returns UpdateOrderItems.Result.Success

    val response = controller.updateOrderItems(request)

    response.status shouldBe HttpStatus.OK
  }

  @Test
  fun `returns 404 Not Found on OrderNotFound`() {
    val request = createUpdateOrderItemsRequest()

    every {
      updateOrderItems.execute(any())
    } returns UpdateOrderItems.Result.Failure.OrderNotFound

    val response = controller.updateOrderItems(request)

    response.status shouldBe HttpStatus.NOT_FOUND
  }

  @Test
  fun `returns 400 Bad Request on Unauthorized`() {
    val request = createUpdateOrderItemsRequest()

    every {
      updateOrderItems.execute(any())
    } returns UpdateOrderItems.Result.Failure.Unauthorized

    val response = controller.updateOrderItems(request)

    response.status shouldBe HttpStatus.BAD_REQUEST
  }

  @Test
  fun `returns 400 Bad Request on EmptyItems`() {
    val request = createUpdateOrderItemsRequest()

    every {
      updateOrderItems.execute(any())
    } returns UpdateOrderItems.Result.Failure.EmptyItems

    val response = controller.updateOrderItems(request)

    response.status shouldBe HttpStatus.BAD_REQUEST
  }
}
