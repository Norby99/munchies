package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.order.application.port.inbound.UpdateOrderItems
import com.munchies.order.fixtures.createUpdateOrderItemsRequest
import com.munchies.order.fixtures.defaultOrderId
import com.munchies.order.infrastructure.adapter.inbound.web.config.OrderServiceConfig
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MockBean
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class UpdateOrderItemsControllerTest : BaseOrderControllerTest() {

  private val updateOrderItemsMock = mockk<UpdateOrderItems>()

  @MockBean(UpdateOrderItems::class)
  fun updateOrderItems(): UpdateOrderItems = updateOrderItemsMock

  // ==========================================
  // TEST: PATCH orders/{id}/items
  // ==========================================

  @Test
  fun `PATCH update order items should return 200 OK on success`() {
    val requestBody = createUpdateOrderItemsRequest()

    every {
      updateOrderItemsMock.execute(any())
    } returns UpdateOrderItems.Result.Success

    val response = client.toBlocking().exchange(
      HttpRequest.PATCH(
        "/${OrderServiceConfig.UPDATE_ORDER_ITEMS_PATH.replace("{id}", defaultOrderId.value)}",
        mapper.writeValueAsString(requestBody),
      ),
      String::class.java,
    )
    response.status shouldBe HttpStatus.OK
  }

  @Test
  fun `PATCH update order items should return 404 Not Found on OrderNotFound`() {
    val requestBody = createUpdateOrderItemsRequest()

    every {
      updateOrderItemsMock.execute(any())
    } returns UpdateOrderItems.Result.Failure.OrderNotFound

    val exception = assertThrows(HttpClientResponseException::class.java) {
      client.toBlocking().exchange(
        HttpRequest.PATCH(
          "/${OrderServiceConfig.UPDATE_ORDER_ITEMS_PATH.replace("{id}", defaultOrderId.value)}",
          mapper.writeValueAsString(requestBody),
        ),
        String::class.java,
      )
    }

    exception.status shouldBe HttpStatus.NOT_FOUND
  }

  @Test
  fun `PATCH update order items should return 400 Bad Request on Unauthorized`() {
    val requestBody = createUpdateOrderItemsRequest()

    every {
      updateOrderItemsMock.execute(any())
    } returns UpdateOrderItems.Result.Failure.Unauthorized

    val exception = assertThrows(HttpClientResponseException::class.java) {
      client.toBlocking().exchange(
        HttpRequest.PATCH(
          "/${OrderServiceConfig.UPDATE_ORDER_ITEMS_PATH.replace("{id}", defaultOrderId.value)}",
          mapper.writeValueAsString(requestBody),
        ),
        String::class.java,
      )
    }

    exception.status shouldBe HttpStatus.BAD_REQUEST
  }

  @Test
  fun `PATCH update order items should return 400 Bad Request on EmptyItems`() {
    val requestBody = createUpdateOrderItemsRequest()

    every {
      updateOrderItemsMock.execute(any())
    } returns UpdateOrderItems.Result.Failure.EmptyItems

    val exception = assertThrows(HttpClientResponseException::class.java) {
      client.toBlocking().exchange(
        HttpRequest.PATCH(
          "/${OrderServiceConfig.UPDATE_ORDER_ITEMS_PATH.replace("{id}", defaultOrderId.value)}",
          mapper.writeValueAsString(requestBody),
        ),
        String::class.java,
      )
    }

    exception.status shouldBe HttpStatus.BAD_REQUEST
  }
}
