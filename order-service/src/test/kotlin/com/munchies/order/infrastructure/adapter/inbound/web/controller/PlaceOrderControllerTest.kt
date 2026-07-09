package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.order.application.port.inbound.PlaceOrder
import com.munchies.order.fixtures.createDeliveryOrder
import com.munchies.order.fixtures.createPlaceOrderRequest
import com.munchies.order.infrastructure.adapter.dto.factory.OrderDtoFactory.toDto
import com.munchies.order.infrastructure.adapter.inbound.web.config.OrderServiceConfig
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MockBean
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class PlaceOrderControllerTest : BaseOrderControllerTest() {

  private val placeOrderMock = mockk<PlaceOrder>()

  @MockBean(PlaceOrder::class)
  fun placeOrder(): PlaceOrder = placeOrderMock

  // ==========================================
  // TEST: POST /orders
  // ==========================================

  @Test
  fun `placeOrder should return 200 OK and order ID on success`() {
    val order = createDeliveryOrder()
    val requestBody = createPlaceOrderRequest(order)

    every {
      placeOrderMock.execute(any())
    } returns PlaceOrder.Result.Success(order.toDto())

    val response = client.toBlocking().exchange(
      HttpRequest.POST(
        "/${OrderServiceConfig.PLACE_ORDER_PATH}",
        mapper.writeValueAsString(requestBody),
      ),
      String::class.java,
    )
    response.status shouldBe HttpStatus.OK
    response.body() shouldBeEqual "Order placed successfully with ID: ${order.id.value}"
  }

  @Test
  fun `placeOrder should return 400 Bad Request on InvalidDate`() {
    val order = createDeliveryOrder()
    val requestBody = createPlaceOrderRequest(order)

    every {
      placeOrderMock.execute(any())
    } returns PlaceOrder.Result.Failure.InvalidDate

    val exception = assertThrows(HttpClientResponseException::class.java) {
      client.toBlocking().exchange(
        HttpRequest.POST(
          "/${OrderServiceConfig.PLACE_ORDER_PATH}",
          mapper.writeValueAsString(requestBody),
        ),
        String::class.java,
      )
    }

    exception.status shouldBe HttpStatus.BAD_REQUEST
  }

  @Test
  fun `placeOrder should return 400 Bad Request on EmptyItems`() {
    val order = createDeliveryOrder()
    val requestBody = createPlaceOrderRequest(order)

    every {
      placeOrderMock.execute(any())
    } returns PlaceOrder.Result.Failure.EmptyItems

    val exception = assertThrows(HttpClientResponseException::class.java) {
      client.toBlocking().exchange(
        HttpRequest.POST(
          "/${OrderServiceConfig.PLACE_ORDER_PATH}",
          mapper.writeValueAsString(requestBody),
        ),
        String::class.java,
      )
    }

    exception.status shouldBe HttpStatus.BAD_REQUEST
  }

  @Test
  fun `placeOrder should return 400 Bad Request on InvalidItemQuantity`() {
    val order = createDeliveryOrder()
    val requestBody = createPlaceOrderRequest(order)

    every {
      placeOrderMock.execute(any())
    } returns PlaceOrder.Result.Failure.InvalidItemQuantity

    val exception = assertThrows(HttpClientResponseException::class.java) {
      client.toBlocking().exchange(
        HttpRequest.POST(
          "/${OrderServiceConfig.PLACE_ORDER_PATH}",
          mapper.writeValueAsString(requestBody),
        ),
        String::class.java,
      )
    }

    exception.status shouldBe HttpStatus.BAD_REQUEST
  }
}
