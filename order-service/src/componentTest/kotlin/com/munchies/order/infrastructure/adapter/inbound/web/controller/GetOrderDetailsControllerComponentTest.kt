package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.order.fixtures.createSampleOrder
import com.munchies.order.fixtures.defaultOrderId
import com.munchies.order.infrastructure.adapter.dto.factory.OrderDtoFactory.toDto
import com.munchies.order.infrastructure.adapter.outbound.mongo.repository.MongoCrudOrderRepository
import com.munchies.order.infrastructure.adapter.outbound.mongo.repository.MongoOrderRepository
import com.munchies.order.infrastructure.adapter.outbound.response.GetOrderDetailsResponse
import com.munchies.order.infrastructure.adapter.outbound.response.GetOrderDetailsResponseType
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

    val response = httpCalls.httpGet<GetOrderDetailsResponse>(realDto.orderId)

    response.status shouldBe HttpStatus.OK
    response.body().code shouldBe HttpStatus.OK.code
    response.body().type shouldBe GetOrderDetailsResponseType.SUCCESS
    response.body().order shouldBe realDto
  }

  @Test
  fun `getOrderDetails should return 404 Not Found when use case returns OrderNotFound`() {
    val response = assertThrows<HttpClientResponseException> {
      httpCalls.httpGet<GetOrderDetailsResponse>(defaultOrderId.value)
    }.response

    response.status shouldBe HttpStatus.NOT_FOUND
    response.bd<GetOrderDetailsResponse>().code shouldBe HttpStatus.NOT_FOUND.code
    response.bd<GetOrderDetailsResponse>().type shouldBe
      GetOrderDetailsResponseType.ORDER_NOT_FOUND
    response.bd<GetOrderDetailsResponse>().order shouldBe null
  }
}
