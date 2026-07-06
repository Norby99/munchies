package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.order.application.port.inbound.AdvanceOrderStatus
import com.munchies.order.fixtures.createAdvanceOrderStatusRequest
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

class AdvanceOrderStatusControllerTest : BaseOrderControllerTest() {

  private val advanceOrderStatusMock = mockk<AdvanceOrderStatus>()

  @MockBean(AdvanceOrderStatus::class)
  fun advanceOrderStatus(): AdvanceOrderStatus = advanceOrderStatusMock

  // ==========================================
  // TEST: POST orders/{id}/advance
  // ==========================================

  @Test
  fun `POST advance order status should return 200 OK on success`() {
    val requestBody = createAdvanceOrderStatusRequest(defaultOrderId)

    every {
      advanceOrderStatusMock.execute(any())
    } returns AdvanceOrderStatus.Result.Success

    val response = client.toBlocking().exchange(
      HttpRequest.POST(
        "/${OrderServiceConfig.ADVANCE_ORDER_STATUS_PATH.replace("{id}", defaultOrderId.value)}",
        mapper.writeValueAsString(requestBody),
      ),
      String::class.java,
    )
    response.status shouldBe HttpStatus.OK
    response.body() shouldBeEqual "Order status advanced"
  }

  @Test
  fun `POST advance order status should return 404 Not Found on OrderNotFound`() {
    val requestBody = createAdvanceOrderStatusRequest(defaultOrderId)

    every {
      advanceOrderStatusMock.execute(any())
    } returns AdvanceOrderStatus.Result.Failure.OrderNotFound

    val exception = assertThrows(HttpClientResponseException::class.java) {
      client.toBlocking().exchange(
        HttpRequest.POST(
          "/${OrderServiceConfig.ADVANCE_ORDER_STATUS_PATH.replace("{id}", defaultOrderId.value)}",
          mapper.writeValueAsString(requestBody),
        ),
        String::class.java,
      )
    }

    exception.status shouldBe HttpStatus.NOT_FOUND
  }

  @Test
  fun `POST advance order status should return 400 Bad Request on InvalidTransition`() {
    val requestBody = createAdvanceOrderStatusRequest(defaultOrderId)

    every {
      advanceOrderStatusMock.execute(any())
    } returns AdvanceOrderStatus.Result.Failure.InvalidTransition

    val exception = assertThrows(HttpClientResponseException::class.java) {
      client.toBlocking().exchange(
        HttpRequest.POST(
          "/${OrderServiceConfig.ADVANCE_ORDER_STATUS_PATH.replace("{id}", defaultOrderId.value)}",
          mapper.writeValueAsString(requestBody),
        ),
        String::class.java,
      )
    }

    exception.status shouldBe HttpStatus.BAD_REQUEST
  }
}
