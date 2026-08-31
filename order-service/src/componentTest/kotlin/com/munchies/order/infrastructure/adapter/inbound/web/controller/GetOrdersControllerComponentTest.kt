package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.commons.infrastructure.adapter.ErrorResponse
import com.munchies.order.fixtures.createSampleOrder
import com.munchies.order.infrastructure.adapter.dto.factory.OrderDtoFactory.toDto
import com.munchies.order.infrastructure.adapter.outbound.mongo.repository.MongoCrudOrderRepository
import com.munchies.order.infrastructure.adapter.outbound.mongo.repository.MongoOrderRepository
import com.munchies.order.infrastructure.adapter.outbound.response.getOrdersResponseFromJson
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@MicronautTest(environments = ["prod"], transactional = false)
class GetOrdersControllerComponentTest : BaseOrderController() {

  @Inject
  lateinit var orderRepository: MongoOrderRepository

  @Inject
  lateinit var mongoCrudOrderRepository: MongoCrudOrderRepository

  @AfterEach
  fun cleanupMongo() {
    mongoCrudOrderRepository.deleteAll()
  }

  // ==========================================
  // TEST: GET /orders?restaurantId={restaurantId}&customerId={userId}&status={status}
  // ==========================================

  @Test
  fun `getOrders should return 200 OK and DTO when found`() {
    val order = createSampleOrder()
    orderRepository.save(order)
    val realDto = order.toDto()

    val response = httpCalls.httpGetOrders<String>(customerId = realDto.customerId)
    val result = getOrdersResponseFromJson(response.body())

    response.status shouldBe HttpStatus.OK
    result.code shouldBe HttpStatus.OK.code
    result.result shouldBe listOf(realDto)
  }

  @Test
  fun `getOrders should filter by restaurantId and status combined`() {
    val order = createSampleOrder()
    orderRepository.save(order)
    val dto = order.toDto()

    val response = httpCalls.httpGetOrders<String>(
      restaurantId = dto.restaurantId,
      status = dto.status,
    )

    getOrdersResponseFromJson(response.body()).result shouldBe listOf(dto)
  }

  @Test
  fun `getOrders should return 404 when no order matches combined filters`() {
    val order = createSampleOrder()
    orderRepository.save(order)

    val response = assertThrows<HttpClientResponseException> {
      httpCalls.httpGetOrders<String>(
        restaurantId = order.toDto().restaurantId,
        customerId = "non-existing-user",
      )
    }.response

    response.status shouldBe HttpStatus.NOT_FOUND
    response.bd<ErrorResponse>().code shouldBe HttpStatus.NOT_FOUND.code
    response.bd<ErrorResponse>().result shouldBe "No orders found matching the provided filters"
  }
}
