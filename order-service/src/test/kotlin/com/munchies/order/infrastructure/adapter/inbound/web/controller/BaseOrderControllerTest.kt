package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.order.application.port.inbound.*
import com.munchies.order.fixtures.createDeliveryOrder
import com.munchies.order.fixtures.createPlaceOrderRequest
import com.munchies.order.fixtures.createSampleOrder
import com.munchies.order.fixtures.defaultOrderId
import com.munchies.order.infrastructure.adapter.dto.OrderDto
import com.munchies.order.infrastructure.adapter.dto.OrderItemDto
import com.munchies.order.infrastructure.adapter.dto.OrderType
import com.munchies.order.infrastructure.adapter.dto.factory.OrderDTOFactory.toDto
import com.munchies.order.infrastructure.adapter.inbound.request.AdvanceOrderStatusRequest
import com.munchies.order.infrastructure.adapter.inbound.request.DiscardOrderRequest
import com.munchies.order.infrastructure.adapter.inbound.request.GetOrderDetailsRequest
import com.munchies.order.infrastructure.adapter.inbound.request.PlaceOrderRequest
import com.munchies.order.infrastructure.adapter.inbound.request.UpdateDeliveryOrderRequest
import com.munchies.order.infrastructure.adapter.inbound.request.UpdateOrderItemsRequest
import com.munchies.order.infrastructure.adapter.inbound.request.UpdateTakeawayOrderRequest
import com.munchies.order.infrastructure.adapter.inbound.web.config.OrderServiceConfig
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.serde.ObjectMapper
import io.micronaut.serde.annotation.SerdeImport
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.mockk.every
import io.mockk.mockk
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

@SerdeImport(OrderDto::class)
@SerdeImport(OrderDto.Delivery::class)
@SerdeImport(OrderDto.Takeaway::class)
@SerdeImport(OrderDto.DineIn::class)
@SerdeImport(OrderItemDto::class)
@SerdeImport(OrderType::class)
@SerdeImport(PlaceOrderRequest::class)
@SerdeImport(GetOrderDetailsRequest::class)
@SerdeImport(AdvanceOrderStatusRequest::class)
@SerdeImport(DiscardOrderRequest::class)
@SerdeImport(UpdateOrderItemsRequest::class)
@SerdeImport(UpdateDeliveryOrderRequest::class)
@SerdeImport(UpdateTakeawayOrderRequest::class)
@MicronautTest
class MicronautOrderControllerTest {

  @Inject
  @field:Client(
    "http://localhost:${OrderServiceConfig.SERVICE_PORT}${OrderServiceConfig.SERVICE_PATH}",
  )
  lateinit var client: HttpClient

  @Inject
  private lateinit var mapper: ObjectMapper

  private val getOrderDetailsMock = mockk<GetOrderDetails>()
  private val advanceOrderStatusMock = mockk<AdvanceOrderStatus>()
  private val placeOrderMock = mockk<PlaceOrder>()
  private val discardOrderMock = mockk<DiscardOrder>()
  private val updateOrderItemsMock = mockk<UpdateOrderItems>()
  private val updateDeliveryOrderInfoMock = mockk<UpdateDeliveryOrderInfo>()
  private val updateTakeawayOrderInfoMock = mockk<UpdateTakeawayOrderInfo>()

  @MockBean(GetOrderDetails::class)
  fun getOrderDetails(): GetOrderDetails = getOrderDetailsMock

  @MockBean(AdvanceOrderStatus::class)
  fun advanceOrderStatus(): AdvanceOrderStatus = advanceOrderStatusMock

  @MockBean(PlaceOrder::class)
  fun placeOrder(): PlaceOrder = placeOrderMock

  @MockBean(DiscardOrder::class)
  fun discardOrder(): DiscardOrder = discardOrderMock

  @MockBean(UpdateOrderItems::class)
  fun updateOrderItems(): UpdateOrderItems = updateOrderItemsMock

  @MockBean(UpdateDeliveryOrderInfo::class)
  fun updateDeliveryOrderInfo(): UpdateDeliveryOrderInfo = updateDeliveryOrderInfoMock

  @MockBean(UpdateTakeawayOrderInfo::class)
  fun updateTakeawayOrderInfo(): UpdateTakeawayOrderInfo = updateTakeawayOrderInfoMock

  // ==========================================
  // TEST: GET /orders/{id}
  // ==========================================

  @Test
  fun `getOrderDetails should return 200 OK and DTO when found`() {
    val realDto = createSampleOrder().toDto()

    every {
      getOrderDetailsMock.execute(any())
    } returns GetOrderDetails.Result.Success(realDto)

    val response = client.toBlocking().exchange(
      HttpRequest.GET<Any>(realDto.orderId),
      OrderDto.Takeaway::class.java,
    )

    response.status shouldBe HttpStatus.OK
    response.body() shouldBeEqual realDto
  }

  @Test
  fun `getOrderDetails should return 404 Not Found when use case returns OrderNotFound`() {
    every {
      getOrderDetailsMock.execute(any())
    } returns GetOrderDetails.Result.Failure.OrderNotFound

    val exception = assertThrows(HttpClientResponseException::class.java) {
      client.toBlocking().exchange<Any, Any>(HttpRequest.GET(defaultOrderId.value))
    }

    exception.status shouldBe HttpStatus.NOT_FOUND
  }

  // ==========================================
  // TEST: POST /orders
  // ==========================================

  @Test
  fun `placeOrder should return 200 OK and order ID on success`() {
    val order = createDeliveryOrder()
    val requestBody = createPlaceOrderRequest(order)

    every {
      placeOrderMock.execute(any())
    } returns PlaceOrder.Result.Success(order.toDto())

    val response = client.toBlocking().exchange(
      HttpRequest.POST(
        "/${OrderServiceConfig.PLACE_ORDER_PATH}",
        mapper.writeValueAsString(requestBody),
      ),
      String::class.java,
    )
    response.status shouldBe HttpStatus.OK
    response.body() shouldBeEqual "Order placed successfully with ID: ${order.id.value}"
  }

  @Test
  fun `placeOrder should return 400 Bad Request on InvalidDate`() {
    val order = createDeliveryOrder()
    val requestBody = createPlaceOrderRequest(order)

    every {
      placeOrderMock.execute(any())
    } returns PlaceOrder.Result.Failure.InvalidDate

    val exception = assertThrows(HttpClientResponseException::class.java) {
      client.toBlocking().exchange(
        HttpRequest.POST(
          "/${OrderServiceConfig.PLACE_ORDER_PATH}",
          mapper.writeValueAsString(requestBody),
        ),
        String::class.java,
      )
    }

    exception.status shouldBe HttpStatus.BAD_REQUEST
  }

  @Test
  fun `placeOrder should return 400 Bad Request on EmptyItems`() {
    val order = createDeliveryOrder()
    val requestBody = createPlaceOrderRequest(order)

    every {
      placeOrderMock.execute(any())
    } returns PlaceOrder.Result.Failure.EmptyItems

    val exception = assertThrows(HttpClientResponseException::class.java) {
      client.toBlocking().exchange(
        HttpRequest.POST(
          "/${OrderServiceConfig.PLACE_ORDER_PATH}",
          mapper.writeValueAsString(requestBody),
        ),
        String::class.java,
      )
    }

    exception.status shouldBe HttpStatus.BAD_REQUEST
  }

  @Test
  fun `placeOrder should return 400 Bad Request on InvalidItemQuantity`() {
    val order = createDeliveryOrder()
    val requestBody = createPlaceOrderRequest(order)

    every {
      placeOrderMock.execute(any())
    } returns PlaceOrder.Result.Failure.InvalidItemQuantity

    val exception = assertThrows(HttpClientResponseException::class.java) {
      client.toBlocking().exchange(
        HttpRequest.POST(
          "/${OrderServiceConfig.PLACE_ORDER_PATH}",
          mapper.writeValueAsString(requestBody),
        ),
        String::class.java,
      )
    }
  }

  // ==========================================
  // TEST: POST orders/{id}/advance
  // ==========================================

  @Test
  fun `POST advance status should return 200 OK on success`() {
    val requestBody = AdvanceOrderStatusRequest(defaultOrderId.value)

    every {
      advanceOrderStatusMock.execute(any())
    } returns AdvanceOrderStatus.Result.Success

    val response = client.toBlocking().exchange(
      HttpRequest.POST(
        "/${OrderServiceConfig.ADVANCE_ORDER_STATUS_PATH}",
        mapper.writeValueAsString(requestBody),
      ),
      String::class.java,
    )

    response.status shouldBe HttpStatus.OK
  }

  @Test
  fun `POST advance status should return 400 Bad Request on InvalidTransition`() {
    val requestBody = AdvanceOrderStatusRequest(defaultOrderId.value)

    every {
      advanceOrderStatusMock.execute(any())
    } returns AdvanceOrderStatus.Result.Failure.InvalidTransition

    val exception = assertThrows(HttpClientResponseException::class.java) {
      client.toBlocking().exchange(
        HttpRequest.POST(
          "/${OrderServiceConfig.ADVANCE_ORDER_STATUS_PATH}",
          requestBody,
        ),
        String::class.java,
      )
    }

    exception.status shouldBe HttpStatus.BAD_REQUEST
  }

  @Test
  fun `POST advance status should return 404 Not Found on OrderNotFound`() {
    val requestBody = AdvanceOrderStatusRequest(defaultOrderId.value)

    every {
      advanceOrderStatusMock.execute(any())
    } returns AdvanceOrderStatus.Result.Failure.OrderNotFound

    val exception = assertThrows(HttpClientResponseException::class.java) {
      client.toBlocking().exchange(
        HttpRequest.POST(
          "/${OrderServiceConfig.ADVANCE_ORDER_STATUS_PATH}",
          requestBody,
        ),
        String::class.java,
      )
    }

    exception.status shouldBe HttpStatus.NOT_FOUND
  }

  // ==========================================
  // TEST: POST orders/{id}/discard
  // ==========================================

  /*
  is DiscardOrder.Result.Success -> HttpResponse.ok("Order discarded")
      is DiscardOrder.Result.Failure.OrderNotFound -> HttpResponse.notFound()
      is DiscardOrder.Result.Failure.Unauthorized ->
        HttpResponse.badRequest("Unauthorized to discard this order")
      is DiscardOrder.Result.Failure.OrderNotCancellable ->
        HttpResponse.badRequest("Cannot discard this order")
   */

}
