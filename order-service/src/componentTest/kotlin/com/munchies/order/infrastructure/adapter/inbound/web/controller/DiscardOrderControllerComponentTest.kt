package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.order.domain.model.OrderStatus
import com.munchies.order.fixtures.createDeliveryOrder
import com.munchies.order.fixtures.createDiscardOrderRequest
import com.munchies.order.fixtures.defaultOrderId
import com.munchies.order.fixtures.secondaryCustomerId
import com.munchies.order.fixtures.secondaryOrderId
import com.munchies.order.infrastructure.adapter.inbound.web.config.OrderServiceConfig
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
class DiscardOrderControllerComponentTest : BaseOrderController() {

  @Inject
  lateinit var orderRepository: MongoOrderRepository

  @Inject
  lateinit var mongoCrudOrderRepository: MongoCrudOrderRepository

  @AfterEach
  fun cleanupMongo() {
    mongoCrudOrderRepository.deleteAll()
  }

  // ==========================================
  // TEST: POST orders/{id}/discard
  // ==========================================

  @Test
  fun `POST discard order should return 200 OK on success`() {
    orderRepository.save(createDeliveryOrder())

    val response = httpPost(
      mapper.writeValueAsString(createDiscardOrderRequest(defaultOrderId)),
      OrderServiceConfig.DISCARD_ORDER_PATH,
    )

    response.status shouldBe HttpStatus.OK
    response.body() shouldBeEqual "Order discarded"
  }

  @Test
  fun `POST discard order should return 404 Not Found on OrderNotFound`() {
    orderRepository.save(createDeliveryOrder())

    val response = assertThrows(HttpClientResponseException::class.java) {
      httpPost(
        mapper.writeValueAsString(createDiscardOrderRequest(secondaryOrderId)),
        OrderServiceConfig.DISCARD_ORDER_PATH,
      )
    }

    response.status shouldBe HttpStatus.NOT_FOUND
  }

  @Test
  fun `POST discard order should return 400 Bad Request on Unauthorized`() {
    orderRepository.save(createDeliveryOrder())

    val response = assertThrows(HttpClientResponseException::class.java) {
      httpPost(
        mapper.writeValueAsString(createDiscardOrderRequest(defaultOrderId, secondaryCustomerId)),
        OrderServiceConfig.DISCARD_ORDER_PATH,
      )
    }

    response.status shouldBe HttpStatus.BAD_REQUEST
  }

  @Test
  fun `POST discard order should return 400 Bad Request on OrderNotCancellable`() {
    orderRepository.save(createDeliveryOrder(status = OrderStatus.COMPLETED))

    val response = assertThrows(HttpClientResponseException::class.java) {
      httpPost(
        mapper.writeValueAsString(createDiscardOrderRequest(defaultOrderId)),
        OrderServiceConfig.DISCARD_ORDER_PATH,
      )
    }

    response.status shouldBe HttpStatus.BAD_REQUEST
  }
}
