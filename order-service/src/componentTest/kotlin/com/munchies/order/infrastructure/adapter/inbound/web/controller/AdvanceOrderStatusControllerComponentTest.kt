package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.order.domain.model.OrderStatus
import com.munchies.order.fixtures.createAdvanceOrderStatusRequest
import com.munchies.order.fixtures.createDeliveryOrder
import com.munchies.order.fixtures.defaultOrderId
import com.munchies.order.infrastructure.adapter.inbound.web.config.OrderServiceConfig
import com.munchies.order.infrastructure.adapter.outbound.mongo.repository.MongoCrudOrderRepository
import com.munchies.order.infrastructure.adapter.outbound.mongo.repository.MongoOrderRepository
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

@MicronautTest(environments = ["prod"], transactional = false)
class AdvanceOrderStatusControllerComponentTest : BaseOrderController() {

  @Inject
  lateinit var orderRepository: MongoOrderRepository

  @Inject
  lateinit var mongoCrudOrderRepository: MongoCrudOrderRepository

  @AfterEach
  fun cleanupMongo() {
    mongoCrudOrderRepository.deleteAll()
  }

  // ==========================================
  // TEST: POST orders/{id}/advance
  // ==========================================

  @Test
  fun `POST advance order status should return 200 OK on success`() {
    orderRepository.save(createDeliveryOrder(status = OrderStatus.PENDING))

    val response = client.toBlocking().exchange(
      HttpRequest.POST(
        "/${OrderServiceConfig.ADVANCE_ORDER_STATUS_PATH.replace("{id}", defaultOrderId.value)}",
        mapper.writeValueAsString(createAdvanceOrderStatusRequest(defaultOrderId)),
      ),
      String::class.java,
    )

    response.status shouldBe HttpStatus.OK
    response.body() shouldBeEqual "Order status advanced"
  }

  @Test
  fun `POST advance order status should return 404 Not Found on OrderNotFound`() {
    val response = assertThrows(HttpClientResponseException::class.java) {
      client.toBlocking().exchange(
        HttpRequest.POST(
          "/${OrderServiceConfig.ADVANCE_ORDER_STATUS_PATH.replace("{id}", defaultOrderId.value)}",
          mapper.writeValueAsString(createAdvanceOrderStatusRequest(defaultOrderId)),
        ),
        String::class.java,
      )
    }

    response.status shouldBe HttpStatus.NOT_FOUND
  }

  @Test
  fun `POST advance order status should return 400 Bad Request on InvalidTransition`() {
    orderRepository.save(createDeliveryOrder(status = OrderStatus.COMPLETED))

    val response = assertThrows(HttpClientResponseException::class.java) {
      client.toBlocking().exchange(
        HttpRequest.POST(
          "/${OrderServiceConfig.ADVANCE_ORDER_STATUS_PATH.replace("{id}", defaultOrderId.value)}",
          mapper.writeValueAsString(createAdvanceOrderStatusRequest(defaultOrderId)),
        ),
        String::class.java,
      )
    }
    response.status shouldBe HttpStatus.BAD_REQUEST
  }
}
