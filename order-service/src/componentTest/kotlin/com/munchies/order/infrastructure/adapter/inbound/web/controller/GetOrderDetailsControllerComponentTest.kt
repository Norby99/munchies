package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.order.fixtures.createSampleOrder
import com.munchies.order.fixtures.defaultOrderId
import com.munchies.order.infrastructure.adapter.dto.factory.OrderDtoFactory.toDto
import com.munchies.order.infrastructure.adapter.outbound.mongo.repository.MongoCrudOrderRepository
import com.munchies.order.infrastructure.adapter.outbound.mongo.repository.MongoOrderRepository
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

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

    val response = httpCalls.httpGet(
      realDto.orderId,
    )

    response.status shouldBe HttpStatus.OK
    response.body() shouldBeEqual realDto
  }

  @Test
  fun `getOrderDetails should return 404 Not Found when use case returns OrderNotFound`() {
    val response = assertThrows(HttpClientResponseException::class.java) {
      httpCalls.httpGet(defaultOrderId.value)
    }

    response.status shouldBe HttpStatus.NOT_FOUND
  }
}
