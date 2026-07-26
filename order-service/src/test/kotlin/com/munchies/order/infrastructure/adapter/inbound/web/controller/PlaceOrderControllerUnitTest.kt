package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.order.application.port.inbound.PlaceOrder
import com.munchies.order.fixtures.createDeliveryOrder
import com.munchies.order.fixtures.createPlaceOrderRequest
import com.munchies.order.infrastructure.adapter.dto.factory.OrderDtoFactory.toDto
import com.munchies.order.infrastructure.adapter.outbound.response.PlaceOrderResponse
import com.munchies.order.infrastructure.adapter.outbound.response.PlaceOrderResponseType
import io.kotest.matchers.nulls.shouldBeNull
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
    response.body().code shouldBe HttpStatus.OK.code
    response.body().type shouldBe PlaceOrderResponseType.SUCCESS
  }

  @Test
  fun `returns 400 Bad Request on InvalidDate`() {
    val order = createDeliveryOrder()
    val request = createPlaceOrderRequest(order)

    every { placeOrder.execute(any()) } returns PlaceOrder.Result.Failure.InvalidDate

    val response = controller.placeOrder(request)

    response.status shouldBe HttpStatus.BAD_REQUEST
    response.bd<PlaceOrderResponse>().code shouldBe HttpStatus.BAD_REQUEST.code
    response.bd<PlaceOrderResponse>().type shouldBe PlaceOrderResponseType.INVALID_DATE
    response.bd<PlaceOrderResponse>().order.shouldBeNull()
  }

  @Test
  fun `returns 400 Bad Request on EmptyItems`() {
    val order = createDeliveryOrder()
    val request = createPlaceOrderRequest(order)

    every { placeOrder.execute(any()) } returns PlaceOrder.Result.Failure.EmptyItems

    val response = controller.placeOrder(request)

    response.status shouldBe HttpStatus.BAD_REQUEST
    response.bd<PlaceOrderResponse>().code shouldBe HttpStatus.BAD_REQUEST.code
    response.bd<PlaceOrderResponse>().type shouldBe PlaceOrderResponseType.EMPTY_ITEMS
    response.bd<PlaceOrderResponse>().order.shouldBeNull()
  }

  @Test
  fun `returns 400 Bad Request on InvalidItemQuantity`() {
    val order = createDeliveryOrder()
    val request = createPlaceOrderRequest(order)

    every { placeOrder.execute(any()) } returns
      PlaceOrder.Result.Failure.InvalidItemQuantity

    val response = controller.placeOrder(request)

    response.status shouldBe HttpStatus.BAD_REQUEST
    response.bd<PlaceOrderResponse>().code shouldBe HttpStatus.BAD_REQUEST.code
    response.bd<PlaceOrderResponse>().type shouldBe PlaceOrderResponseType.INVALID_ITEM_QUANTITY
    response.bd<PlaceOrderResponse>().order.shouldBeNull()
  }
}
