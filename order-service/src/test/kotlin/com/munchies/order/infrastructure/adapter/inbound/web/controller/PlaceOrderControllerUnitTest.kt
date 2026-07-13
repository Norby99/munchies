package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.order.application.port.inbound.PlaceOrder
import com.munchies.order.fixtures.createDeliveryOrder
import com.munchies.order.fixtures.createPlaceOrderRequest
import com.munchies.order.infrastructure.adapter.dto.factory.OrderDtoFactory.toDto
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import io.mockk.every
import org.junit.jupiter.api.Test

class PlaceOrderControllerUnitTest : BaseOrderController() {

  @Test
  fun `returns 200 OK and order ID on success`() {
    val order = createDeliveryOrder()
    val request = createPlaceOrderRequest(order)

    every { placeOrder.execute(any()) } returns PlaceOrder.Result.Success(order.toDto())

    val response = controller.placeOrder(request)

    response.status shouldBe HttpStatus.OK
    response.body() shouldBeEqual "Order placed successfully with ID: ${order.id.value}"
  }

  @Test
  fun `returns 400 Bad Request on InvalidDate`() {
    val order = createDeliveryOrder()
    val request = createPlaceOrderRequest(order)

    every { placeOrder.execute(any()) } returns PlaceOrder.Result.Failure.InvalidDate

    val response = controller.placeOrder(request)

    response.status shouldBe HttpStatus.BAD_REQUEST
  }

  @Test
  fun `returns 400 Bad Request on EmptyItems`() {
    val order = createDeliveryOrder()
    val request = createPlaceOrderRequest(order)

    every { placeOrder.execute(any()) } returns PlaceOrder.Result.Failure.EmptyItems

    val response = controller.placeOrder(request)

    response.status shouldBe HttpStatus.BAD_REQUEST
  }

  @Test
  fun `returns 400 Bad Request on InvalidItemQuantity`() {
    val order = createDeliveryOrder()
    val request = createPlaceOrderRequest(order)

    every { placeOrder.execute(any()) } returns PlaceOrder.Result.Failure.InvalidItemQuantity

    val response = controller.placeOrder(request)

    response.status shouldBe HttpStatus.BAD_REQUEST
  }
}
