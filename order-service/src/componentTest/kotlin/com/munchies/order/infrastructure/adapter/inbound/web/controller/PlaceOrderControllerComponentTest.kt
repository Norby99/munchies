package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.order.domain.model.OrderId
import com.munchies.order.fixtures.createDeliveryInfo
import com.munchies.order.fixtures.createDeliveryOrder
import com.munchies.order.fixtures.createEmptyItems
import com.munchies.order.fixtures.createInvalidItemsNegativeCount
import com.munchies.order.fixtures.createPlaceOrderRequest
import com.munchies.order.fixtures.pastTime
import com.munchies.order.infrastructure.adapter.inbound.web.config.OrderServiceConfig
import com.munchies.order.infrastructure.adapter.outbound.mongo.repository.MongoCrudOrderRepository
import com.munchies.order.infrastructure.adapter.outbound.mongo.repository.MongoOrderRepository
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

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

    val response = client.toBlocking().exchange(
      HttpRequest.POST(
        "/${OrderServiceConfig.PLACE_ORDER_PATH}",
        mapper.writeValueAsString(requestBody),
      ),
      String::class.java,
    )

    val generatedId =
      OrderId(response.body()!!.substringAfter("Order placed successfully with ID: "))
    val savedOrder = orderRepository.findById(generatedId)

    response.status shouldBe HttpStatus.OK

    savedOrder shouldBe order.copy(id = generatedId)
  }

  @Test
  fun `placeOrder should return 400 Bad Request on InvalidDate`() {
    val order =
      createDeliveryOrder(deliveryInfo = createDeliveryInfo(estimatedDeliveryTime = pastTime))
    val requestBody = createPlaceOrderRequest(order)

    val response = assertThrows(HttpClientResponseException::class.java) {
      client.toBlocking().exchange(
        HttpRequest.POST(
          "/${OrderServiceConfig.PLACE_ORDER_PATH}",
          mapper.writeValueAsString(requestBody),
        ),
        String::class.java,
      )
    }

    response.status shouldBe HttpStatus.BAD_REQUEST
  }

  @Test
  fun `placeOrder should return 400 Bad Request on EmptyItems`() {
    val order = createDeliveryOrder(items = createEmptyItems())
    val requestBody = createPlaceOrderRequest(order)

    val response = assertThrows(HttpClientResponseException::class.java) {
      client.toBlocking().exchange(
        HttpRequest.POST(
          "/${OrderServiceConfig.PLACE_ORDER_PATH}",
          mapper.writeValueAsString(requestBody),
        ),
        String::class.java,
      )
    }

    response.status shouldBe HttpStatus.BAD_REQUEST
  }

  @Test
  fun `placeOrder should return 400 Bad Request on InvalidItemQuantity`() {
    val order = createDeliveryOrder(items = createInvalidItemsNegativeCount())
    val requestBody = createPlaceOrderRequest(order)

    val response = assertThrows(HttpClientResponseException::class.java) {
      client.toBlocking().exchange(
        HttpRequest.POST(
          "/${OrderServiceConfig.PLACE_ORDER_PATH}",
          mapper.writeValueAsString(requestBody),
        ),
        String::class.java,
      )
    }

    response.status shouldBe HttpStatus.BAD_REQUEST
  }
}
