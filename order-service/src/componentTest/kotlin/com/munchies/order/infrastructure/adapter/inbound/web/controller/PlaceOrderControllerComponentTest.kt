package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.commons.infrastructure.adapter.ErrorResponse
import com.munchies.order.fixtures.createDeliveryInfo
import com.munchies.order.fixtures.createDeliveryOrder
import com.munchies.order.fixtures.createEmptyItems
import com.munchies.order.fixtures.createInvalidItemsNegativeCount
import com.munchies.order.fixtures.createPlaceOrderRequest
import com.munchies.order.fixtures.pastTime
import com.munchies.order.infrastructure.adapter.inbound.web.config.OrderServiceConfig
import com.munchies.order.infrastructure.adapter.outbound.mongo.repository.MongoCrudOrderRepository
import com.munchies.order.infrastructure.adapter.outbound.mongo.repository.MongoOrderRepository
import com.munchies.order.infrastructure.adapter.outbound.response.PlaceOrderResponse
import com.munchies.order.infrastructure.adapter.outbound.response.placeOrderResponseFromJson
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@MicronautTest(environments = ["prod"], transactional = false)
class PlaceOrderControllerComponentTest : BaseOrderController() {

  @Inject
  lateinit var orderRepository: MongoOrderRepository

  @Inject
  lateinit var mongoCrudOrderRepository: MongoCrudOrderRepository

  @AfterEach
  fun cleanupMongo() {
    mongoCrudOrderRepository.deleteAll()
  }

  // ==========================================
  // TEST: POST /orders
  // ==========================================

  @Test
  fun `placeOrder should return 200 OK and order ID on success`() {
    val order = createDeliveryOrder()
    val requestBody = createPlaceOrderRequest(order)

    val response = httpCalls.httpPost<String>(
      mapper.writeValueAsString(requestBody),
      OrderServiceConfig.PLACE_ORDER_PATH,
    )

    val result = placeOrderResponseFromJson(response.body())

    response.status shouldBe HttpStatus.OK
    result.code shouldBe HttpStatus.OK.code
  }

  @Test
  fun `placeOrder should return 400 Bad Request on InvalidDate`() {
    val order =
      createDeliveryOrder(deliveryInfo = createDeliveryInfo(estimatedDeliveryTime = pastTime))
    val requestBody = createPlaceOrderRequest(order)

    val response = assertThrows<HttpClientResponseException> {
      httpCalls.httpPost<PlaceOrderResponse>(
        mapper.writeValueAsString(requestBody),
        OrderServiceConfig.PLACE_ORDER_PATH,
      )
    }.response

    response.status shouldBe HttpStatus.BAD_REQUEST
    response.bd<ErrorResponse>().code shouldBe HttpStatus.BAD_REQUEST.code
    response.bd<ErrorResponse>().result shouldBe "Invalid date"

    orderRepository.findById(order.id).shouldBeNull()
  }

  @Test
  fun `placeOrder should return 400 Bad Request on EmptyItems`() {
    val order = createDeliveryOrder(items = createEmptyItems())
    val requestBody = createPlaceOrderRequest(order)

    val response = assertThrows<HttpClientResponseException> {
      httpCalls.httpPost<PlaceOrderResponse>(
        mapper.writeValueAsString(requestBody),
        OrderServiceConfig.PLACE_ORDER_PATH,
      )
    }.response

    response.status shouldBe HttpStatus.BAD_REQUEST
    response.bd<ErrorResponse>().code shouldBe HttpStatus.BAD_REQUEST.code
    response.bd<ErrorResponse>().result shouldBe "Empty items"

    orderRepository.findById(order.id).shouldBeNull()
  }

  @Test
  fun `placeOrder should return 400 Bad Request on InvalidItemQuantity`() {
    val order = createDeliveryOrder(items = createInvalidItemsNegativeCount())
    val requestBody = createPlaceOrderRequest(order)

    val response = assertThrows<HttpClientResponseException> {
      httpCalls.httpPost<PlaceOrderResponse>(
        mapper.writeValueAsString(requestBody),
        OrderServiceConfig.PLACE_ORDER_PATH,
      )
    }.response

    response.status shouldBe HttpStatus.BAD_REQUEST
    response.bd<ErrorResponse>().code shouldBe HttpStatus.BAD_REQUEST.code
    response.bd<ErrorResponse>().result shouldBe "Invalid item quantity"

    orderRepository.findById(order.id).shouldBeNull()
  }
}
