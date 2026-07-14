package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.order.domain.model.DeliveryOrder
import com.munchies.order.fixtures.Address1
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
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

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

    val response = httpPatch(
      mapper.writeValueAsString(requestBody),
      OrderServiceConfig.UPDATE_DELIVERY_ORDER_INFO_PATH,
    )

    response.status shouldBe HttpStatus.OK

    initialOrder.deliveryInfo.deliveryAddress shouldBe Address1.deliveryAddress
    initialOrder.deliveryInfo.bellName shouldBe Address1.bellName
    initialOrder.deliveryInfo.customerPhone shouldBe Address1.customerPhone

    val updatedOrder = orderRepository.findById(defaultOrderId) as DeliveryOrder

    updatedOrder.deliveryInfo.deliveryAddress shouldBe Address2.deliveryAddress
    updatedOrder.deliveryInfo.bellName shouldBe Address2.bellName
    updatedOrder.deliveryInfo.customerPhone shouldBe Address2.customerPhone
  }

  @Test
  fun `PATCH update delivery order info should return 404 Not Found on OrderNotFound`() {
    val initialOrder = createDeliveryOrder()
    val newOrder = initialOrder.copy(id = secondaryOrderId)

    orderRepository.save(initialOrder)

    val requestBody = createUpdateDeliveryOrderRequest(newOrder)

    val response = assertThrows(HttpClientResponseException::class.java) {
      httpPatch(
        mapper.writeValueAsString(requestBody),
        OrderServiceConfig.UPDATE_DELIVERY_ORDER_INFO_PATH,
      )
    }

    response.status shouldBe HttpStatus.NOT_FOUND
  }

  @Test
  fun `PATCH update delivery order info should return 400 Bad Request on Unauthorized`() {
    val initialOrder = createDeliveryOrder()
    val newOrder = initialOrder.copy(customerId = secondaryCustomerId)

    orderRepository.save(initialOrder)

    val requestBody = createUpdateDeliveryOrderRequest(newOrder)

    val response = assertThrows(HttpClientResponseException::class.java) {
      httpPatch(
        mapper.writeValueAsString(requestBody),
        OrderServiceConfig.UPDATE_DELIVERY_ORDER_INFO_PATH,
      )
    }

    response.status shouldBe HttpStatus.BAD_REQUEST
  }

  @Test
  fun `PATCH update delivery order info should return 400 Bad Request on InvalidDate`() {
    val initialOrder = createDeliveryOrder()
    val newOrder = initialOrder.copy(
      deliveryInfo = initialOrder.deliveryInfo.copy(estimatedDeliveryTime = pastTime),
    )

    orderRepository.save(initialOrder)

    val requestBody = createUpdateDeliveryOrderRequest(newOrder)

    val response = assertThrows(HttpClientResponseException::class.java) {
      httpPatch(
        mapper.writeValueAsString(requestBody),
        OrderServiceConfig.UPDATE_DELIVERY_ORDER_INFO_PATH,
      )
    }

    response.status shouldBe HttpStatus.BAD_REQUEST
  }
}
