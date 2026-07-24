package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.order.domain.model.DeliveryOrder
import com.munchies.order.domain.model.OrderId
import com.munchies.order.domain.model.TakeawayOrder
import com.munchies.order.fixtures.Address2
import com.munchies.order.fixtures.createTakeawayOrder
import com.munchies.order.fixtures.createUpdateTakeawayOrderRequest
import com.munchies.order.fixtures.defaultOrderId
import com.munchies.order.fixtures.pastTime
import com.munchies.order.fixtures.secondaryCustomerId
import com.munchies.order.infrastructure.adapter.inbound.web.config.OrderServiceConfig
import com.munchies.order.infrastructure.adapter.outbound.mongo.repository.MongoCrudOrderRepository
import com.munchies.order.infrastructure.adapter.outbound.mongo.repository.MongoOrderRepository
import com.munchies.order.infrastructure.adapter.outbound.response.UpdateDeliveryOrderResponse
import com.munchies.order.infrastructure.adapter.outbound.response.UpdateDeliveryOrderResponseType
import com.munchies.order.infrastructure.adapter.outbound.response.UpdateTakeawayOrderResponse
import com.munchies.order.infrastructure.adapter.outbound.response.UpdateTakeawayOrderResponseType
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.equals.shouldNotBeEqual
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@MicronautTest(environments = ["prod"], transactional = false)
class UpdateTakeawayOrderInfoControllerComponentTest : BaseOrderController() {

  @Inject
  lateinit var orderRepository: MongoOrderRepository

  @Inject
  lateinit var mongoCrudOrderRepository: MongoCrudOrderRepository

  @AfterEach
  fun cleanupMongo() {
    mongoCrudOrderRepository.deleteAll()
  }

  @Test
  fun `PATCH update takeaway order info should return 200 OK on success`() {
    val initialOrder = createTakeawayOrder()
    val newOrder = initialOrder.copy(
      takeawayInfo = initialOrder.takeawayInfo.copy(customerName = Address2.bellName),
    )

    orderRepository.save(initialOrder)

    val requestBody = createUpdateTakeawayOrderRequest(newOrder)

    val response = httpCalls.httpPatch<UpdateTakeawayOrderResponse>(
      mapper.writeValueAsString(requestBody),
      OrderServiceConfig.UPDATE_TAKEAWAY_ORDER_INFO_PATH,
    )

    response.status shouldBe HttpStatus.OK
    response.body().code shouldBe HttpStatus.OK.code
    response.body().type shouldBe UpdateTakeawayOrderResponseType.SUCCESS

    val updatedOrder = orderRepository.findById(defaultOrderId) as TakeawayOrder
    updatedOrder shouldBeEqual newOrder
  }

  @Test
  fun `PATCH update takeaway order info should return 404 Not Found on OrderNotFound`() {
    val requestBody = createUpdateTakeawayOrderRequest()

    val response = assertThrows<HttpClientResponseException> {
      httpCalls.httpPatch<UpdateTakeawayOrderResponse>(
        mapper.writeValueAsString(requestBody),
        OrderServiceConfig.UPDATE_TAKEAWAY_ORDER_INFO_PATH,
      )
    }.response

    response.status shouldBe HttpStatus.NOT_FOUND
    response.bd<UpdateTakeawayOrderResponse>().code shouldBe HttpStatus.NOT_FOUND.code
    response.bd<UpdateTakeawayOrderResponse>().type shouldBe UpdateTakeawayOrderResponseType.ORDER_NOT_FOUND

    orderRepository.findById(OrderId(requestBody.orderId)).shouldBeNull()
  }

  @Test
  fun `PATCH update takeaway order info should return 400 Bad Request on Unauthorized`() {
    val initialOrder = createTakeawayOrder()
    val newOrder = initialOrder.copy(customerId = secondaryCustomerId)

    orderRepository.save(initialOrder)

    val requestBody = createUpdateTakeawayOrderRequest(newOrder)

    val response = assertThrows<HttpClientResponseException> {
      httpCalls.httpPatch<UpdateTakeawayOrderResponse>(
        mapper.writeValueAsString(requestBody),
        OrderServiceConfig.UPDATE_TAKEAWAY_ORDER_INFO_PATH,
      )
    }.response

    response.status shouldBe HttpStatus.BAD_REQUEST
    response.bd<UpdateTakeawayOrderResponse>().code shouldBe HttpStatus.UNAUTHORIZED.code
    response.bd<UpdateTakeawayOrderResponse>().type shouldBe UpdateTakeawayOrderResponseType.UNAUTHORIZED

    val updatedOrder = orderRepository.findById(defaultOrderId) as TakeawayOrder
    updatedOrder shouldNotBeEqual newOrder
  }

  @Test
  fun `PATCH update takeaway order info should return 400 Bad Request on InvalidDate`() {
    val initialOrder = createTakeawayOrder()
    val newOrder = initialOrder.copy(
      takeawayInfo = initialOrder.takeawayInfo.copy(
        pickupTime = pastTime,
      ),
    )

    orderRepository.save(initialOrder)

    val requestBody = createUpdateTakeawayOrderRequest(newOrder)

    val response = assertThrows<HttpClientResponseException> {
      httpCalls.httpPatch<UpdateTakeawayOrderResponse>(
        mapper.writeValueAsString(requestBody),
        OrderServiceConfig.UPDATE_TAKEAWAY_ORDER_INFO_PATH,
      )
    }.response

    response.status shouldBe HttpStatus.BAD_REQUEST
    response.bd<UpdateTakeawayOrderResponse>().code shouldBe HttpStatus.BAD_REQUEST.code
    response.bd<UpdateTakeawayOrderResponse>().type shouldBe UpdateTakeawayOrderResponseType.INVALID_DATE

    val updatedOrder = orderRepository.findById(defaultOrderId) as TakeawayOrder
    updatedOrder shouldNotBeEqual newOrder
  }
}
