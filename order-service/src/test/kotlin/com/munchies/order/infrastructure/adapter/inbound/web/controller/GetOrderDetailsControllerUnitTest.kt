package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.order.application.port.inbound.GetOrderDetails
import com.munchies.order.fixtures.createSampleOrder
import com.munchies.order.fixtures.defaultOrderId
import com.munchies.order.infrastructure.adapter.dto.factory.OrderDtoFactory.toDto
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import io.mockk.every
import org.junit.jupiter.api.Test

class GetOrderDetailsControllerUnitTest : BaseOrderController() {

  @Test
  fun `returns 200 OK and DTO when found`() {
    val realDto = createSampleOrder().toDto()
    every { getOrderDetails.execute(any()) } returns GetOrderDetails.Result.Success(realDto)

    val response = controller.getOrderDetails(realDto.orderId)

    response.status shouldBe HttpStatus.OK
    /*response.body().code shouldBe HttpStatus.OK.code
    response.body().result.code shouldBe GetOrderCode.Success
    response.body().result.order?.shouldBeEqual(realDto)*/
  }

  @Test
  fun `returns 404 Not Found when use case returns OrderNotFound`() {
    every { getOrderDetails.execute(any()) } returns GetOrderDetails.Result.Failure.OrderNotFound

    val response = controller.getOrderDetails(defaultOrderId.value)

    response.status shouldBe HttpStatus.NOT_FOUND
    /*response.body().code shouldBe HttpStatus.NOT_FOUND.code
    response.body().result.code shouldBe GetOrderCode.OrderNotFound
    response.body().result.order shouldBe null*/
  }

  /*@Test
  fun `serializes and deserializes correctly`() {
    val realDto = createSampleOrder().toDto()
    val response =
      GetOrderResponse(GetOrderResultSuccess(realDto), HttpStatus.OK.code)
    val json = response.toJson()
    val decoded = getOrderResponseFromJson(json)
    decoded.code shouldBe response.code
    decoded.result.shouldBeInstanceOf<GetOrderResultSuccess>()
  }*/
}
