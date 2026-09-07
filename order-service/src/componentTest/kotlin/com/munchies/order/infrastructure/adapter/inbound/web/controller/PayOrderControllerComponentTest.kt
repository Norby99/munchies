package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.order.domain.model.DeliveryOrder
import com.munchies.order.fixtures.createDeliveryOrder
import com.munchies.order.infrastructure.adapter.inbound.web.config.OrderServiceConfig
import com.munchies.order.infrastructure.adapter.outbound.mongo.repository.MongoCrudOrderRepository
import com.munchies.order.infrastructure.adapter.outbound.mongo.repository.MongoOrderRepository
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@MicronautTest(environments = ["prod"], transactional = false)
class PayOrderControllerComponentTest : BaseOrderController() {

  @Inject
  lateinit var orderRepository: MongoOrderRepository

  @Inject
  lateinit var mongoCrudOrderRepository: MongoCrudOrderRepository

  @AfterEach
  fun cleanupMongo() {
    mongoCrudOrderRepository.deleteAll()
  }

  // ==========================================
  // TEST: POST orders/{id}/pay
  // ==========================================

  @Test
  fun `PATCH pay order should return 200 OK on success`() {
    val order = createDeliveryOrder()
    orderRepository.save(order)

    val response = httpCalls.httpPatch<String>(
      "",
      OrderServiceConfig.PAY_ORDER_PATH.replace("{id}", order.id.value),
    )

    response.status shouldBe HttpStatus.OK

    val updatedOrder = orderRepository.findById(order.id) as DeliveryOrder
    updatedOrder.payed shouldBe true
  }

  @Test
  fun `PATCH pay order should return 400 Bad Request when order is already paid`() {
    val order = createDeliveryOrder().copy(payed = true)
    orderRepository.save(order)

    val response = assertThrows<HttpClientResponseException> {
      httpCalls.httpPatch<String>(
        "",
        OrderServiceConfig.PAY_ORDER_PATH.replace("{id}", order.id.value),
      )
    }

    response.status shouldBe HttpStatus.BAD_REQUEST
  }

  @Test
  fun `PATCH pay order should return 404 Not Found when order does not exist`() {
    val nonExistentOrderId = "non-existent-order-id"
    val order = createDeliveryOrder()
    orderRepository.save(order)

    val response = assertThrows<HttpClientResponseException> {
      httpCalls.httpPatch<String>(
        "",
        OrderServiceConfig.PAY_ORDER_PATH.replace("{id}", nonExistentOrderId),
      )
    }

    response.status shouldBe HttpStatus.NOT_FOUND
  }
}
