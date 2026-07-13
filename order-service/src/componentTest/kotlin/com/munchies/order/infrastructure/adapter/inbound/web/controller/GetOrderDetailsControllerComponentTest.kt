package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.order.application.port.inbound.GetOrderDetails
import com.munchies.order.fixtures.createSampleOrder
import com.munchies.order.fixtures.defaultOrderId
import com.munchies.order.infrastructure.adapter.dto.OrderDto
import com.munchies.order.infrastructure.adapter.dto.factory.OrderDtoFactory.toDto
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

class GetOrderDetailsControllerComponentTest : BaseOrderController() {

  private val getOrderDetailsMock = mockk<GetOrderDetails>()

  @MockBean(GetOrderDetails::class)
  fun getOrderDetails(): GetOrderDetails = getOrderDetailsMock

  // ==========================================
  // TEST: GET /orders/{id}
  // ==========================================

  @Test
  fun `getOrderDetails should return 200 OK and DTO when found`() {
    val realDto = createSampleOrder().toDto()
    every { getOrderDetailsMock.execute(any()) } returns GetOrderDetails.Result.Success(realDto)

    val response = client.toBlocking().exchange(
      HttpRequest.GET<Any>(realDto.orderId),
      OrderDto.Takeaway::class.java,
    )

    response.status shouldBe HttpStatus.OK
    response.body() shouldBeEqual realDto
  }

  @Test
  fun `getOrderDetails should return 404 Not Found when use case returns OrderNotFound`() {
    every {
      getOrderDetailsMock.execute(
        any(),
      )
    } returns GetOrderDetails.Result.Failure.OrderNotFound

    val exception = assertThrows(HttpClientResponseException::class.java) {
      client.toBlocking().exchange<Any, Any>(HttpRequest.GET(defaultOrderId.value))
    }

    exception.status shouldBe HttpStatus.NOT_FOUND
  }
}
