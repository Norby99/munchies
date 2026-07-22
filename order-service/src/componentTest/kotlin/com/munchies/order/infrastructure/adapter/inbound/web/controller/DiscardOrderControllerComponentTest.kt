package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.order.domain.model.OrderStatus
import com.munchies.order.fixtures.createDeliveryOrder
import com.munchies.order.fixtures.defaultOrderId
import com.munchies.order.fixtures.secondaryOrderId
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
  fun `DELETE discard order should return 200 OK on success`() {
    val id = defaultOrderId.value
    orderRepository.save(createDeliveryOrder())

    val response = httpCalls.httpDelete(id)

    response.status shouldBe HttpStatus.OK
    response.body() shouldBeEqual "Order discarded"
  }

  @Test
  fun `DELETE discard order should return 404 Not Found on OrderNotFound`() {
    val id = secondaryOrderId.value
    orderRepository.save(createDeliveryOrder())

    val response = assertThrows(HttpClientResponseException::class.java) {
      httpCalls.httpDelete(id)
    }

    response.status shouldBe HttpStatus.NOT_FOUND
  }

  @Test
  fun `DELETE discard order should return 400 Bad Request on OrderNotCancellable`() {
    val id = defaultOrderId.value
    orderRepository.save(createDeliveryOrder(status = OrderStatus.COMPLETED))

    val response = assertThrows(HttpClientResponseException::class.java) {
      httpCalls.httpDelete(id)
    }

    response.status shouldBe HttpStatus.BAD_REQUEST
  }
}
