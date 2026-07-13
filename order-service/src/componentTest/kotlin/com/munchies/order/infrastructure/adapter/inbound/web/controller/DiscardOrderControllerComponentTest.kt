package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.order.application.port.inbound.DiscardOrder
import com.munchies.order.fixtures.createDiscardOrderRequest
import com.munchies.order.fixtures.defaultOrderId
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

class DiscardOrderControllerComponentTest : BaseOrderController() {

  private val discardOrderMock = mockk<DiscardOrder>()

  @MockBean(DiscardOrder::class)
  fun discardOrder(): DiscardOrder = discardOrderMock

  // ==========================================
  // TEST: POST orders/{id}/discard
  // ==========================================

  @Test
  fun `POST discard order should return 200 OK on success`() {
    val requestBody = createDiscardOrderRequest(defaultOrderId)

    every {
      discardOrderMock.execute(any())
    } returns DiscardOrder.Result.Success

    val response = client.toBlocking().exchange(
      HttpRequest.POST(
        "/${OrderServiceConfig.DISCARD_ORDER_PATH.replace("{id}", defaultOrderId.value)}",
        mapper.writeValueAsString(requestBody),
      ),
      String::class.java,
    )
    response.status shouldBe HttpStatus.OK
    response.body() shouldBeEqual "Order discarded"
  }

  @Test
  fun `POST discard order should return 404 Not Found on OrderNotFound`() {
    val requestBody = createDiscardOrderRequest(defaultOrderId)

    every {
      discardOrderMock.execute(any())
    } returns DiscardOrder.Result.Failure.OrderNotFound

    val exception = assertThrows(HttpClientResponseException::class.java) {
      client.toBlocking().exchange(
        HttpRequest.POST(
          "/${OrderServiceConfig.DISCARD_ORDER_PATH.replace("{id}", defaultOrderId.value)}",
          mapper.writeValueAsString(requestBody),
        ),
        String::class.java,
      )
    }

    exception.status shouldBe HttpStatus.NOT_FOUND
  }

  @Test
  fun `POST discard order should return 400 Bad Request on Unauthorized`() {
    val requestBody = createDiscardOrderRequest(defaultOrderId)

    every {
      discardOrderMock.execute(any())
    } returns DiscardOrder.Result.Failure.Unauthorized

    val exception = assertThrows(HttpClientResponseException::class.java) {
      client.toBlocking().exchange(
        HttpRequest.POST(
          "/${OrderServiceConfig.DISCARD_ORDER_PATH.replace("{id}", defaultOrderId.value)}",
          mapper.writeValueAsString(requestBody),
        ),
        String::class.java,
      )
    }

    exception.status shouldBe HttpStatus.BAD_REQUEST
  }

  @Test
  fun `POST discard order should return 400 Bad Request on OrderNotCancellable`() {
    val requestBody = createDiscardOrderRequest(defaultOrderId)

    every {
      discardOrderMock.execute(any())
    } returns DiscardOrder.Result.Failure.OrderNotCancellable

    val exception = assertThrows(HttpClientResponseException::class.java) {
      client.toBlocking().exchange(
        HttpRequest.POST(
          "/${OrderServiceConfig.DISCARD_ORDER_PATH.replace("{id}", defaultOrderId.value)}",
          mapper.writeValueAsString(requestBody),
        ),
        String::class.java,
      )
    }

    exception.status shouldBe HttpStatus.BAD_REQUEST
  }
}
