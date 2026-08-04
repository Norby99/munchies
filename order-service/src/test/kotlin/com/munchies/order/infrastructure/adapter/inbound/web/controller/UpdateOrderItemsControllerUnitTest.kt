package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.commons.domain.port.ValidationException
import com.munchies.order.application.port.inbound.UpdateOrderItems
import com.munchies.order.fixtures.createUpdateOrderItemsRequest
import com.munchies.order.infrastructure.adapter.inbound.web.controller.exception.NotFoundException
import com.munchies.order.infrastructure.adapter.inbound.web.controller.exception.UnauthorizedException
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import io.mockk.every
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UpdateOrderItemsControllerUnitTest : BaseOrderController() {

  @Test
  fun `returns 200 OK on success`() {
    val request = createUpdateOrderItemsRequest()

    every {
      updateOrderItems.execute(any())
    } returns UpdateOrderItems.Result.Success

    val response = controller.updateOrderItems(request)

    response.status shouldBe HttpStatus.OK
    response.body().code shouldBe HttpStatus.OK.code
    response.body().result shouldBe "Order items updated successfully"
  }

  @Test
  fun `throws NotFoundException on OrderNotFound`() {
    val request = createUpdateOrderItemsRequest()

    every {
      updateOrderItems.execute(any())
    } returns UpdateOrderItems.Result.Failure.OrderNotFound

    val exception = assertThrows<NotFoundException> {
      controller.updateOrderItems(request)
    }
    exception.message shouldBe "Order not found"
  }

  @Test
  fun `throws UnauthorizedException on Unauthorized`() {
    val request = createUpdateOrderItemsRequest()

    every {
      updateOrderItems.execute(any())
    } returns UpdateOrderItems.Result.Failure.Unauthorized

    val exception = assertThrows<UnauthorizedException> {
      controller.updateOrderItems(request)
    }
    exception.message shouldBe "Unauthorized"
  }

  @Test
  fun `throws ValidationException on EmptyItems`() {
    val request = createUpdateOrderItemsRequest()

    every {
      updateOrderItems.execute(any())
    } returns UpdateOrderItems.Result.Failure.EmptyItems

    val exception = assertThrows<ValidationException> {
      controller.updateOrderItems(request)
    }
    exception.message shouldBe "Empty items"
  }
}
