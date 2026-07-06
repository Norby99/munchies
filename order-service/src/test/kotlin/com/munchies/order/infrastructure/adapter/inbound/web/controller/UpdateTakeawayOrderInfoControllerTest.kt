package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.order.application.port.inbound.UpdateTakeawayOrderInfo
import com.munchies.order.fixtures.createUpdateTakeawayOrderRequest
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

class UpdateTakeawayOrderInfoControllerTest : BaseOrderControllerTest() {

  private val updateTakeawayOrderInfoMock = mockk<UpdateTakeawayOrderInfo>()

  @MockBean(UpdateTakeawayOrderInfo::class)
  fun updateTakeawayOrderInfo(): UpdateTakeawayOrderInfo = updateTakeawayOrderInfoMock

  // ==========================================
  // TEST: PATCH orders/{id}/takeaway
  // ==========================================

  @Test
  fun `PATCH update takeaway order info should return 200 OK on success`() {
    val requestBody = createUpdateTakeawayOrderRequest(defaultOrderId)

    every {
      updateTakeawayOrderInfoMock.execute(any())
    } returns UpdateTakeawayOrderInfo.Result.Success

    val response = client.toBlocking().exchange(
      HttpRequest.PATCH(
        "/${
          OrderServiceConfig.UPDATE_TAKEAWAY_ORDER_INFO_PATH.replace(
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
  fun `PATCH update takeaway order info should return 404 Not Found on OrderNotFound`() {
    val requestBody = createUpdateTakeawayOrderRequest(defaultOrderId)

    every {
      updateTakeawayOrderInfoMock.execute(any())
    } returns UpdateTakeawayOrderInfo.Result.Failure.OrderNotFound

    val exception = assertThrows(HttpClientResponseException::class.java) {
      client.toBlocking().exchange(
        HttpRequest.PATCH(
          "/${
            OrderServiceConfig.UPDATE_TAKEAWAY_ORDER_INFO_PATH.replace(
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
  fun `PATCH update takeaway order info should return 400 Bad Request on Unauthorized`() {
    val requestBody = createUpdateTakeawayOrderRequest(defaultOrderId)

    every {
      updateTakeawayOrderInfoMock.execute(any())
    } returns UpdateTakeawayOrderInfo.Result.Failure.Unauthorized

    val exception = assertThrows(HttpClientResponseException::class.java) {
      client.toBlocking().exchange(
        HttpRequest.PATCH(
          "/${
            OrderServiceConfig.UPDATE_TAKEAWAY_ORDER_INFO_PATH.replace(
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
  fun `PATCH update takeaway order info should return 400 Bad Request on InvalidDate`() {
    val requestBody = createUpdateTakeawayOrderRequest(defaultOrderId)

    every {
      updateTakeawayOrderInfoMock.execute(any())
    } returns UpdateTakeawayOrderInfo.Result.Failure.InvalidDate

    val exception = assertThrows(HttpClientResponseException::class.java) {
      client.toBlocking().exchange(
        HttpRequest.PATCH(
          "/${
            OrderServiceConfig.UPDATE_TAKEAWAY_ORDER_INFO_PATH.replace(
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
