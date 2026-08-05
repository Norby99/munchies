package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.commons.infrastructure.adapter.ErrorResponse
import com.munchies.order.domain.model.DeliveryOrder
import com.munchies.order.fixtures.Address2
import com.munchies.order.fixtures.createDeliveryOrder
import com.munchies.order.fixtures.createUpdateDeliveryOrderRequest
import com.munchies.order.fixtures.defaultOrderId
import com.munchies.order.fixtures.pastTime
import com.munchies.order.fixtures.secondaryCustomerId
import com.munchies.order.fixtures.secondaryOrderId
import com.munchies.order.infrastructure.adapter.inbound.web.config.OrderServiceConfig
import com.munchies.order.infrastructure.adapter.outbound.mongo.repository.MongoCrudOrderRepository
import com.munchies.order.infrastructure.adapter.outbound.mongo.repository.MongoOrderRepository
import com.munchies.order.infrastructure.adapter.outbound.response.UpdateDeliveryOrderResponse
import com.munchies.order.infrastructure.adapter.outbound.response.updateDeliveryOrderResponseFromJson
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.equals.shouldNotBeEqual
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@MicronautTest(environments = ["prod"], transactional = false)
class UpdateDeliveryOrderInfoControllerComponentTest : BaseOrderController() {

  @Inject
  lateinit var orderRepository: MongoOrderRepository

  @Inject
  lateinit var mongoCrudOrderRepository: MongoCrudOrderRepository

  @AfterEach
  fun cleanupMongo() {
    mongoCrudOrderRepository.deleteAll()
  }

  // ==========================================
  // TEST: PATCH orders/{id}/delivery
  // ==========================================

  @Test
  fun `PATCH update delivery order info should return 200 OK on success`() {
    val initialOrder = createDeliveryOrder()
    val newOrder = initialOrder.copy(
      deliveryInfo = initialOrder.deliveryInfo.copy(
        deliveryAddress = Address2.deliveryAddress,
        bellName = Address2.bellName,
        customerPhone = Address2.customerPhone,
      ),
    )

    orderRepository.save(initialOrder)

    val requestBody = createUpdateDeliveryOrderRequest(newOrder)

    val response = httpCalls.httpPatch<String>(
      mapper.writeValueAsString(requestBody),
      OrderServiceConfig.UPDATE_DELIVERY_ORDER_INFO_PATH,
    )

    val result = updateDeliveryOrderResponseFromJson(response.body())

    response.status shouldBe HttpStatus.OK
    result.code shouldBe HttpStatus.OK.code

    val updatedOrder = orderRepository.findById(defaultOrderId) as DeliveryOrder
    updatedOrder shouldBeEqual newOrder
  }

  @Test
  fun `PATCH update delivery order info should return 404 Not Found on OrderNotFound`() {
    val initialOrder = createDeliveryOrder()
    val newOrder = initialOrder.copy(id = secondaryOrderId)

    orderRepository.save(initialOrder)

    val requestBody = createUpdateDeliveryOrderRequest(newOrder)

    val response = assertThrows<HttpClientResponseException> {
      httpCalls.httpPatch<UpdateDeliveryOrderResponse>(
        mapper.writeValueAsString(requestBody),
        OrderServiceConfig.UPDATE_DELIVERY_ORDER_INFO_PATH,
      )
    }.response

    response.status shouldBe HttpStatus.NOT_FOUND
    response.bd<ErrorResponse>().code shouldBe HttpStatus.NOT_FOUND.code
    response.bd<ErrorResponse>().result shouldBe "Order not found"

    val updatedOrder = orderRepository.findById(defaultOrderId) as DeliveryOrder
    updatedOrder shouldNotBeEqual newOrder
  }

  @Test
  fun `PATCH update delivery order info should return 401 Unauthorized on Unauthorized`() {
    val initialOrder = createDeliveryOrder()
    val newOrder = initialOrder.copy(customerId = secondaryCustomerId)

    orderRepository.save(initialOrder)

    val requestBody = createUpdateDeliveryOrderRequest(newOrder)

    val response = assertThrows<HttpClientResponseException> {
      httpCalls.httpPatch<UpdateDeliveryOrderResponse>(
        mapper.writeValueAsString(requestBody),
        OrderServiceConfig.UPDATE_DELIVERY_ORDER_INFO_PATH,
      )
    }.response

    response.status shouldBe HttpStatus.UNAUTHORIZED
    response.bd<ErrorResponse>().code shouldBe HttpStatus.UNAUTHORIZED.code
    response.bd<ErrorResponse>().result shouldBe "Unauthorized"

    val updatedOrder = orderRepository.findById(defaultOrderId) as DeliveryOrder
    updatedOrder shouldNotBeEqual newOrder
  }

  @Test
  fun `PATCH update delivery order info should return 400 Bad Request on InvalidDate`() {
    val initialOrder = createDeliveryOrder()
    val newOrder = initialOrder.copy(
      deliveryInfo = initialOrder.deliveryInfo.copy(estimatedDeliveryTime = pastTime),
    )

    orderRepository.save(initialOrder)

    val requestBody = createUpdateDeliveryOrderRequest(newOrder)

    val response = assertThrows<HttpClientResponseException> {
      httpCalls.httpPatch<UpdateDeliveryOrderResponse>(
        mapper.writeValueAsString(requestBody),
        OrderServiceConfig.UPDATE_DELIVERY_ORDER_INFO_PATH,
      )
    }.response

    response.status shouldBe HttpStatus.BAD_REQUEST
    response.bd<ErrorResponse>().code shouldBe HttpStatus.BAD_REQUEST.code
    response.bd<ErrorResponse>().result shouldBe "Invalid date"

    val updatedOrder = orderRepository.findById(defaultOrderId) as DeliveryOrder
    updatedOrder shouldNotBeEqual newOrder
  }
}
