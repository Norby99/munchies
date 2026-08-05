package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.commons.domain.port.ValidationException
import com.munchies.order.application.port.inbound.PlaceOrder
import com.munchies.order.fixtures.createDeliveryOrder
import com.munchies.order.fixtures.createPlaceOrderRequest
import com.munchies.order.infrastructure.adapter.dto.factory.OrderDtoFactory.toDto
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import io.mockk.every
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PlaceOrderControllerUnitTest : BaseOrderController() {

  @Test
  fun `returns 200 OK and order ID on success`() {
    val order = createDeliveryOrder()
    val request = createPlaceOrderRequest(order)

    every { placeOrder.execute(any()) } returns PlaceOrder.Result.Success(order.toDto())

    val response = controller.placeOrder(request)

    response.status shouldBe HttpStatus.OK
    response.body().code shouldBe HttpStatus.OK.code
    response.body().result shouldBe order.toDto()
  }

  @Test
  fun `throws ValidationException on InvalidDate`() {
    val order = createDeliveryOrder()
    val request = createPlaceOrderRequest(order)

    every { placeOrder.execute(any()) } returns PlaceOrder.Result.Failure.InvalidDate

    val exception = assertThrows<ValidationException> {
      controller.placeOrder(request)
    }
    exception.message shouldBe "Invalid date"
  }

  @Test
  fun `throws ValidationException on EmptyItems`() {
    val order = createDeliveryOrder()
    val request = createPlaceOrderRequest(order)

    every { placeOrder.execute(any()) } returns PlaceOrder.Result.Failure.EmptyItems

    val exception = assertThrows<ValidationException> {
      controller.placeOrder(request)
    }
    exception.message shouldBe "Empty items"
  }

  @Test
  fun `throws ValidationException on InvalidItemQuantity`() {
    val order = createDeliveryOrder()
    val request = createPlaceOrderRequest(order)

    every { placeOrder.execute(any()) } returns
      PlaceOrder.Result.Failure.InvalidItemQuantity

    val exception = assertThrows<ValidationException> {
      controller.placeOrder(request)
    }
    exception.message shouldBe "Invalid item quantity"
  }
}
