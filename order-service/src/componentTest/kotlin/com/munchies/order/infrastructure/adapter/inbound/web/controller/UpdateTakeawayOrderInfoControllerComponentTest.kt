package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.order.domain.model.TakeawayOrder
import com.munchies.order.fixtures.Address2
import com.munchies.order.fixtures.createTakeawayOrder
import com.munchies.order.fixtures.createUpdateTakeawayOrderRequest
import com.munchies.order.fixtures.pastTime
import com.munchies.order.fixtures.secondaryCustomerId
import com.munchies.order.infrastructure.adapter.inbound.web.config.OrderServiceConfig
import com.munchies.order.infrastructure.adapter.outbound.mongo.repository.MongoCrudOrderRepository
import com.munchies.order.infrastructure.adapter.outbound.mongo.repository.MongoOrderRepository
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

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

    val response = httpCalls.httpPatch(
      mapper.writeValueAsString(requestBody),
      OrderServiceConfig.UPDATE_TAKEAWAY_ORDER_INFO_PATH,
    )

    response.status shouldBe HttpStatus.OK

    val updatedOrder = orderRepository.findById(initialOrder.id) as TakeawayOrder
    updatedOrder.takeawayInfo shouldBe newOrder.takeawayInfo
    updatedOrder.takeawayInfo shouldNotBe initialOrder.takeawayInfo
  }

  @Test
  fun `PATCH update takeaway order info should return 404 Not Found on OrderNotFound`() {
    val requestBody = createUpdateTakeawayOrderRequest()

    val response = assertThrows(HttpClientResponseException::class.java) {
      httpCalls.httpPatch(
        mapper.writeValueAsString(requestBody),
        OrderServiceConfig.UPDATE_TAKEAWAY_ORDER_INFO_PATH,
      )
    }

    response.status shouldBe HttpStatus.NOT_FOUND
  }

  @Test
  fun `PATCH update takeaway order info should return 400 Bad Request on Unauthorized`() {
    val initialOrder = createTakeawayOrder()
    val newOrder = initialOrder.copy(customerId = secondaryCustomerId)

    orderRepository.save(initialOrder)

    val requestBody = createUpdateTakeawayOrderRequest(newOrder)

    val response = assertThrows(HttpClientResponseException::class.java) {
      httpCalls.httpPatch(
        mapper.writeValueAsString(requestBody),
        OrderServiceConfig.UPDATE_TAKEAWAY_ORDER_INFO_PATH,
      )
    }

    response.status shouldBe HttpStatus.BAD_REQUEST
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

    val response = assertThrows(HttpClientResponseException::class.java) {
      httpCalls.httpPatch(
        mapper.writeValueAsString(requestBody),
        OrderServiceConfig.UPDATE_TAKEAWAY_ORDER_INFO_PATH,
      )
    }

    response.status shouldBe HttpStatus.BAD_REQUEST
  }
}
