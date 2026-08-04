package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.commons.infrastructure.adapter.ErrorResponse
import com.munchies.order.fixtures.createSampleOrder
import com.munchies.order.fixtures.defaultOrderId
import com.munchies.order.infrastructure.adapter.dto.factory.OrderDtoFactory.toDto
import com.munchies.order.infrastructure.adapter.outbound.mongo.repository.MongoCrudOrderRepository
import com.munchies.order.infrastructure.adapter.outbound.mongo.repository.MongoOrderRepository
import com.munchies.order.infrastructure.adapter.outbound.response.GetOrderDetailsResponse
import com.munchies.order.infrastructure.adapter.outbound.response.getOrderDetailsResponseFromJson
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@MicronautTest(environments = ["prod"], transactional = false)
class GetOrderDetailsControllerComponentTest : BaseOrderController() {

  @Inject
  lateinit var orderRepository: MongoOrderRepository

  @Inject
  lateinit var mongoCrudOrderRepository: MongoCrudOrderRepository

  @AfterEach
  fun cleanupMongo() {
    mongoCrudOrderRepository.deleteAll()
  }

  // ==========================================
  // TEST: GET /orders/{id}
  // ==========================================

  @Test
  fun `getOrderDetails should return 200 OK and DTO when found`() {
    val order = createSampleOrder()
    orderRepository.save(order)

    val realDto = order.toDto()

    val response = httpCalls.httpGet<String>(realDto.orderId)

    val result = getOrderDetailsResponseFromJson(response.body())

    response.status shouldBe HttpStatus.OK
    result.code shouldBe HttpStatus.OK.code
  }

  @Test
  fun `getOrderDetails should return 404 Not Found when use case returns OrderNotFound`() {
    val response = assertThrows<HttpClientResponseException> {
      httpCalls.httpGet<GetOrderDetailsResponse>(defaultOrderId.value)
    }.response

    response.status shouldBe HttpStatus.NOT_FOUND
    response.bd<ErrorResponse>().code shouldBe HttpStatus.NOT_FOUND.code
    response.bd<ErrorResponse>().result shouldBe "Order not found"
  }
}
