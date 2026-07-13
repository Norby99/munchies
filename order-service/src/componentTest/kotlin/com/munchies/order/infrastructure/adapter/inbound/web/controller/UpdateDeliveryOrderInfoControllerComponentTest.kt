package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.order.application.port.inbound.UpdateDeliveryOrderInfo
import com.munchies.order.fixtures.createUpdateDeliveryOrderRequest
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

class UpdateDeliveryOrderInfoControllerComponentTest : BaseOrderController() {

  private val updateDeliveryOrderInfoMock = mockk<UpdateDeliveryOrderInfo>()

  @MockBean(UpdateDeliveryOrderInfo::class)
  fun updateDeliveryOrderInfo(): UpdateDeliveryOrderInfo = updateDeliveryOrderInfoMock

  // ==========================================
  // TEST: PATCH orders/{id}/delivery
  // ==========================================

  @Test
  fun `PATCH update delivery order info should return 200 OK on success`() {
    val requestBody = createUpdateDeliveryOrderRequest()

    every {
      updateDeliveryOrderInfoMock.execute(any())
    } returns UpdateDeliveryOrderInfo.Result.Success

    val response = client.toBlocking().exchange(
      HttpRequest.PATCH(
        "/${
          OrderServiceConfig.UPDATE_DELIVERY_ORDER_INFO_PATH.replace(
            "{id}",
            defaultOrderId.value,
          )
        }",
        mapper.writeValueAsString(requestBody),
      ),
      String::class.java,
    )
    response.status shouldBe HttpStatus.OK
  }

  @Test
  fun `PATCH update delivery order info should return 404 Not Found on OrderNotFound`() {
    val requestBody = createUpdateDeliveryOrderRequest()

    every {
      updateDeliveryOrderInfoMock.execute(any())
    } returns UpdateDeliveryOrderInfo.Result.Failure.OrderNotFound

    val exception = assertThrows(HttpClientResponseException::class.java) {
      client.toBlocking().exchange(
        HttpRequest.PATCH(
          "/${
            OrderServiceConfig.UPDATE_DELIVERY_ORDER_INFO_PATH.replace(
              "{id}",
              defaultOrderId.value,
            )
          }",
          mapper.writeValueAsString(requestBody),
        ),
        String::class.java,
      )
    }

    exception.status shouldBe HttpStatus.NOT_FOUND
  }

  @Test
  fun `PATCH update delivery order info should return 400 Bad Request on Unauthorized`() {
    val requestBody = createUpdateDeliveryOrderRequest()

    every {
      updateDeliveryOrderInfoMock.execute(any())
    } returns UpdateDeliveryOrderInfo.Result.Failure.Unauthorized

    val exception = assertThrows(HttpClientResponseException::class.java) {
      client.toBlocking().exchange(
        HttpRequest.PATCH(
          "/${
            OrderServiceConfig.UPDATE_DELIVERY_ORDER_INFO_PATH.replace(
              "{id}",
              defaultOrderId.value,
            )
          }",
          mapper.writeValueAsString(requestBody),
        ),
        String::class.java,
      )
    }

    exception.status shouldBe HttpStatus.BAD_REQUEST
  }

  @Test
  fun `PATCH update delivery order info should return 400 Bad Request on InvalidDate`() {
    val requestBody = createUpdateDeliveryOrderRequest()

    every {
      updateDeliveryOrderInfoMock.execute(any())
    } returns UpdateDeliveryOrderInfo.Result.Failure.InvalidDate

    val exception = assertThrows(HttpClientResponseException::class.java) {
      client.toBlocking().exchange(
        HttpRequest.PATCH(
          "/${
            OrderServiceConfig.UPDATE_DELIVERY_ORDER_INFO_PATH.replace(
              "{id}",
              defaultOrderId.value,
            )
          }",
          mapper.writeValueAsString(requestBody),
        ),
        String::class.java,
      )
    }

    exception.status shouldBe HttpStatus.BAD_REQUEST
  }
}
