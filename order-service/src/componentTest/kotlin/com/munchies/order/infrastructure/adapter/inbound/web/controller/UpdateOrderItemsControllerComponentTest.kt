package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.order.domain.model.DeliveryOrder
import com.munchies.order.fixtures.createDeliveryOrder
import com.munchies.order.fixtures.createEmptyItems
import com.munchies.order.fixtures.createItemsDto
import com.munchies.order.fixtures.createNewItems
import com.munchies.order.fixtures.createNewItemsBigger
import com.munchies.order.fixtures.createUpdateOrderItemsRequest
import com.munchies.order.fixtures.secondaryCustomerId
import com.munchies.order.infrastructure.adapter.inbound.web.config.OrderServiceConfig
import com.munchies.order.infrastructure.adapter.outbound.mongo.repository.MongoCrudOrderRepository
import com.munchies.order.infrastructure.adapter.outbound.mongo.repository.MongoOrderRepository
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

@MicronautTest(environments = ["prod"], transactional = false)
class UpdateOrderItemsControllerComponentTest : BaseOrderController() {

  @Inject
  lateinit var orderRepository: MongoOrderRepository

  @Inject
  lateinit var mongoCrudOrderRepository: MongoCrudOrderRepository

  @AfterEach
  fun cleanupMongo() {
    mongoCrudOrderRepository.deleteAll()
  }

  // ==========================================
  // TEST: PATCH orders/{id}/items
  // ==========================================

  @Test
  fun `PATCH update order items should return 200 OK on success`() {
    val initialOrder = createDeliveryOrder(items = createNewItems())

    orderRepository.save(initialOrder)

    val requestBody =
      createUpdateOrderItemsRequest(initialOrder, createItemsDto(createNewItemsBigger()))

    val response = httpCalls.httpPatch(
      mapper.writeValueAsString(requestBody),
      OrderServiceConfig.UPDATE_ORDER_ITEMS_PATH,
    )

    response.status shouldBe HttpStatus.OK

    val updatedOrder = orderRepository.findById(initialOrder.id) as DeliveryOrder
    updatedOrder.items shouldBe createNewItemsBigger()
  }

  @Test
  fun `PATCH update order items should return 404 Not Found on OrderNotFound`() {
    val requestBody = createUpdateOrderItemsRequest()

    val response = assertThrows(HttpClientResponseException::class.java) {
      httpCalls.httpPatch(
        mapper.writeValueAsString(requestBody),
        OrderServiceConfig.UPDATE_ORDER_ITEMS_PATH,
      )
    }

    response.status shouldBe HttpStatus.NOT_FOUND
  }

  @Test
  fun `PATCH update order items should return 400 Bad Request on Unauthorized`() {
    val initialOrder = createDeliveryOrder()
    val newOrder = initialOrder.copy(customerId = secondaryCustomerId)

    orderRepository.save(initialOrder)

    val requestBody = createUpdateOrderItemsRequest(newOrder)

    val response = assertThrows(HttpClientResponseException::class.java) {
      httpCalls.httpPatch(
        mapper.writeValueAsString(requestBody),
        OrderServiceConfig.UPDATE_ORDER_ITEMS_PATH,
      )
    }

    response.status shouldBe HttpStatus.BAD_REQUEST
  }

  @Test
  fun `PATCH update order items should return 400 Bad Request on EmptyItems`() {
    val initialOrder = createDeliveryOrder(items = createNewItems())

    orderRepository.save(initialOrder)

    val requestBody =
      createUpdateOrderItemsRequest(initialOrder, createItemsDto(createEmptyItems()))

    val response = assertThrows(HttpClientResponseException::class.java) {
      httpCalls.httpPatch(
        mapper.writeValueAsString(requestBody),
        OrderServiceConfig.UPDATE_ORDER_ITEMS_PATH,
      )
    }

    response.status shouldBe HttpStatus.BAD_REQUEST
  }
}
