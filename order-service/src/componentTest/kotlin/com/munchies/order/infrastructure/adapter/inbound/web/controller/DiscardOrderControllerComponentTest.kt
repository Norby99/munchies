package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.commons.infrastructure.adapter.ErrorResponse
import com.munchies.order.domain.model.OrderStatus
import com.munchies.order.fixtures.createDeliveryOrder
import com.munchies.order.fixtures.defaultOrderId
import com.munchies.order.fixtures.secondaryOrderId
import com.munchies.order.infrastructure.adapter.outbound.mongo.repository.MongoCrudOrderRepository
import com.munchies.order.infrastructure.adapter.outbound.mongo.repository.MongoOrderRepository
import com.munchies.order.infrastructure.adapter.outbound.response.DiscardOrderResponse
import com.munchies.order.infrastructure.adapter.outbound.response.discardOrderResponseFromJson
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

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

    orderRepository.findById(defaultOrderId).shouldNotBeNull()

    val response = httpCalls.httpDelete<String>(id)

    val result = discardOrderResponseFromJson(response.body())

    response.status shouldBe HttpStatus.OK
    result.code shouldBe HttpStatus.OK.code

    orderRepository.findById(defaultOrderId).shouldBeNull()
  }

  @Test
  fun `DELETE discard order should return 404 Not Found on OrderNotFound`() {
    val id = secondaryOrderId.value
    orderRepository.save(createDeliveryOrder())

    val response = assertThrows<HttpClientResponseException> {
      httpCalls.httpDelete<DiscardOrderResponse>(id)
    }.response

    response.status shouldBe HttpStatus.NOT_FOUND
    response.bd<ErrorResponse>().code shouldBe HttpStatus.NOT_FOUND.code
    response.bd<ErrorResponse>().result shouldBe "Order not found"
  }

  @Test
  fun `DELETE discard order should return 400 Bad Request on OrderNotCancellable`() {
    val id = defaultOrderId.value
    orderRepository.save(createDeliveryOrder(status = OrderStatus.COMPLETED))

    val response = assertThrows<HttpClientResponseException> {
      httpCalls.httpDelete<DiscardOrderResponse>(id)
    }.response

    response.status shouldBe HttpStatus.BAD_REQUEST
    response.bd<ErrorResponse>().code shouldBe HttpStatus.BAD_REQUEST.code
    response.bd<ErrorResponse>().result shouldBe "Order cannot be cancelled"
  }
}
