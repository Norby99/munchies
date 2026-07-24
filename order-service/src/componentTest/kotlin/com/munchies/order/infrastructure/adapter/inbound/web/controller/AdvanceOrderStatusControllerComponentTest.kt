package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.order.domain.model.DeliveryOrder
import com.munchies.order.domain.model.OrderStatus
import com.munchies.order.fixtures.createAdvanceOrderStatusRequest
import com.munchies.order.fixtures.createDeliveryOrder
import com.munchies.order.fixtures.defaultOrderId
import com.munchies.order.infrastructure.adapter.inbound.web.config.OrderServiceConfig
import com.munchies.order.infrastructure.adapter.outbound.mongo.repository.MongoCrudOrderRepository
import com.munchies.order.infrastructure.adapter.outbound.mongo.repository.MongoOrderRepository
import com.munchies.order.infrastructure.adapter.outbound.response.AdvanceOrderStatusResponse
import com.munchies.order.infrastructure.adapter.outbound.response.AdvanceOrderStatusResponseType
import com.munchies.order.infrastructure.adapter.outbound.response.PlaceOrderResponse
import com.munchies.order.infrastructure.adapter.outbound.response.PlaceOrderResponseType
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

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
    val order = createDeliveryOrder(status = OrderStatus.PENDING)
    orderRepository.save(order)

    val response = httpCalls.httpPost<AdvanceOrderStatusResponse>(
      createAdvanceOrderStatusRequest(defaultOrderId),
      OrderServiceConfig.ADVANCE_ORDER_STATUS_PATH,
    )

    response.status shouldBe HttpStatus.OK
    response.body().code shouldBe HttpStatus.OK.code
    response.body().type shouldBe AdvanceOrderStatusResponseType.SUCCESS

    val updatedOrder = orderRepository.findById(order.id) as DeliveryOrder
    updatedOrder.status shouldBe OrderStatus.PREPARING
  }

  @Test
  fun `POST advance order status should return 404 Not Found on OrderNotFound`() {
    val response = assertThrows<HttpClientResponseException> {
      httpCalls.httpPost<AdvanceOrderStatusResponse>(
        mapper.writeValueAsString(createAdvanceOrderStatusRequest(defaultOrderId)),
        OrderServiceConfig.ADVANCE_ORDER_STATUS_PATH,
      )
    }.response

    response.status shouldBe HttpStatus.NOT_FOUND
    response.bd<AdvanceOrderStatusResponse>().code shouldBe HttpStatus.NOT_FOUND.code
    response.bd<AdvanceOrderStatusResponse>().type shouldBe AdvanceOrderStatusResponseType.ORDER_NOT_FOUND
  }

  @Test
  fun `POST advance order status should return 400 Bad Request on InvalidTransition`() {
    orderRepository.save(createDeliveryOrder(status = OrderStatus.COMPLETED))

    val response = assertThrows<HttpClientResponseException> {
      httpCalls.httpPost<AdvanceOrderStatusResponse>(
        mapper.writeValueAsString(createAdvanceOrderStatusRequest(defaultOrderId)),
        OrderServiceConfig.ADVANCE_ORDER_STATUS_PATH,
      )
    }.response

    response.status shouldBe HttpStatus.BAD_REQUEST
    response.bd<AdvanceOrderStatusResponse>().code shouldBe HttpStatus.BAD_REQUEST.code
    response.bd<AdvanceOrderStatusResponse>().type shouldBe AdvanceOrderStatusResponseType.INVALID_TRANSACTION
  }
}
