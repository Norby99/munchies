package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.commons.infrastructure.adapter.ErrorResponse
import com.munchies.order.domain.model.MenuItemId
import com.munchies.order.domain.model.OrderId
import com.munchies.order.domain.model.OrderItem
import com.munchies.order.domain.model.Price
import com.munchies.order.domain.model.event.OrderPlaced
import com.munchies.order.fixtures.createDeliveryInfo
import com.munchies.order.fixtures.createDeliveryOrder
import com.munchies.order.fixtures.createEmptyItems
import com.munchies.order.fixtures.createInvalidItemsNegativeCount
import com.munchies.order.fixtures.createPlaceOrderRequest
import com.munchies.order.fixtures.createTestPlaceOrderRequest
import com.munchies.order.fixtures.menuPrices
import com.munchies.order.fixtures.pastTime
import com.munchies.order.infrastructure.adapter.dto.OrderItemDto
import com.munchies.order.infrastructure.adapter.dto.OrderType
import com.munchies.order.infrastructure.adapter.dto.factory.OrderDtoFactory.toDto
import com.munchies.order.infrastructure.adapter.inbound.web.config.OrderServiceConfig
import com.munchies.order.infrastructure.adapter.outbound.response.PlaceOrderResponse
import com.munchies.order.infrastructure.adapter.outbound.response.placeOrderResponseFromJson
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@MicronautTest(environments = ["prod"], transactional = false)
class PlaceOrderControllerComponentTest : BaseOrderController() {

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

    // The order stream starts with OrderPlaced and the read model already serves the new order.
    val placedId = OrderId(result.result.orderId)
    eventStore.load(placedId).single().shouldBeInstanceOf<OrderPlaced>()
    orderViews.findById(placedId)?.toDto() shouldBe result.result
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

  @Test
  fun `placeOrder should price the items from restaurant-service, ignoring client prices`() {
    // The client tries to set its own (lower) prices: they must be ignored.
    val requestBody = createPlaceOrderRequest(createDeliveryOrder()).let { request ->
      createTestPlaceOrderRequest(
        orderType = OrderType.DELIVERY,
        restaurantId = request.restaurantId,
        customerId = request.customerId,
        items = request.items.map { OrderItemDto(it.menuItemId, it.quantity, unitPriceCents = 1) },
        estimatedDeliveryTime = request.estimatedDeliveryTime!!.toLong(),
        deliveryAddress = request.deliveryAddress,
        bellName = request.bellName,
        customerPhone = request.customerPhone,
      )
    }

    val response = httpCalls.httpPost<String>(
      mapper.writeValueAsString(requestBody),
      OrderServiceConfig.PLACE_ORDER_PATH,
    )

    val placed = placeOrderResponseFromJson(response.body()).result
    val expected = menuPrices()
    placed.items.forEach {
      it.unitPriceCents.toLong() shouldBe expected.getValue(MenuItemId(it.menuItemId)).cents
    }
    // 2 x 9.50 + 3 x 4.00
    placed.totalCents shouldBe 3100
    // The prices are stored with the order: the read model serves the same total.
    orderViews.findById(OrderId(placed.orderId))?.toDto()?.totalCents shouldBe 3100
  }

  @Test
  fun `placeOrder should return 400 Bad Request when an item is not on the menu`() {
    val requestBody = createPlaceOrderRequest(
      createDeliveryOrder(items = listOf(OrderItem(MenuItemId("not-on-menu"), 1, Price.ZERO))),
    )

    val response = assertThrows<HttpClientResponseException> {
      httpCalls.httpPost<PlaceOrderResponse>(
        mapper.writeValueAsString(requestBody),
        OrderServiceConfig.PLACE_ORDER_PATH,
      )
    }.response

    response.status shouldBe HttpStatus.BAD_REQUEST
    response.bd<ErrorResponse>().result shouldBe "Items not on the restaurant's menu: not-on-menu"
  }

  @Test
  fun `placeOrder should return 503 Service Unavailable when restaurant-service fails`() {
    restaurantService.failing = true
    val requestBody = createPlaceOrderRequest(createDeliveryOrder())

    val response = assertThrows<HttpClientResponseException> {
      httpCalls.httpPost<PlaceOrderResponse>(
        mapper.writeValueAsString(requestBody),
        OrderServiceConfig.PLACE_ORDER_PATH,
      )
    }.response

    response.status shouldBe HttpStatus.SERVICE_UNAVAILABLE
    eventCollection.countDocuments() shouldBe 0
  }
}
