package com.munchies.order.infrastructure.adapter.inbound

import com.munchies.commons.domain.port.AuthRole
import com.munchies.commons.infrastructure.adapter.ErrorResponse
import com.munchies.commons.infrastructure.adapter.HttpMethod
import com.munchies.commons.infrastructure.adapter.SimpleAPI
import com.munchies.commons.infrastructure.adapter.WebResponse
import com.munchies.commons.infrastructure.adapter.errorResponseFromJson
import com.munchies.order.infrastructure.adapter.inbound.request.*
import com.munchies.order.infrastructure.adapter.inbound.web.config.OrderServiceConfig
import com.munchies.order.infrastructure.adapter.outbound.response.*
import kotlin.js.JsExport
import kotlin.js.Promise

@JsExport
abstract class JsGetOrderDetailsAPI<E : WebResponse<Any>> :
  OrderAPI.GetOrderDetailsAPI<Promise<E>>,
  SimpleAPI<Nothing, GetOrderDetailsResponse>() {
  override fun getPath(): String =
    OrderServiceConfig.SERVICE_PATH + OrderServiceConfig.GET_ORDER_PATH

  override fun getPort(): Int = OrderServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.GET
  override fun getRequiredAuthRole(): AuthRole = AuthRole.CUSTOMER
  abstract override fun getOrderDetails(id: String): Promise<E>

  override fun parseRequest(json: String): Nothing = throw UnsupportedOperationException()
  override fun parseResponse(json: String): GetOrderDetailsResponse =
    getOrderDetailsResponseFromJson(json)

  override fun parseError(json: String): ErrorResponse = errorResponseFromJson(json)
}

@JsExport
abstract class JsGetOrdersAPI<E : WebResponse<Any>> :
  OrderAPI.GetOrdersAPI<Promise<E>>,
  SimpleAPI<Nothing, GetOrdersResponse>() {
  override fun getPath(): String =
    OrderServiceConfig.SERVICE_PATH + OrderServiceConfig.GET_ORDER_PATH

  override fun getPort(): Int = OrderServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.GET
  override fun getRequiredAuthRole(): AuthRole = AuthRole.CUSTOMER
  abstract override fun getOrders(
    restaurantId: String?,
    customerId: String?,
    orderStatus: String?
  ): Promise<E>

  override fun parseRequest(json: String): Nothing = throw UnsupportedOperationException()
  override fun parseResponse(json: String): GetOrdersResponse =
    getOrdersResponseFromJson(json)

  override fun parseError(json: String): ErrorResponse = errorResponseFromJson(json)
}

@JsExport
abstract class JsPlaceOrderAPI<E : WebResponse<Any>> :
  OrderAPI.PlaceOrderAPI<Promise<E>>,
  SimpleAPI<PlaceOrderRequest, PlaceOrderResponse>() {
  override fun getPath(): String =
    OrderServiceConfig.SERVICE_PATH + OrderServiceConfig.PLACE_ORDER_PATH

  override fun getPort(): Int = OrderServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.POST
  override fun getRequiredAuthRole(): AuthRole = AuthRole.CUSTOMER
  abstract override fun placeOrder(request: PlaceOrderRequest): Promise<E>

  override fun parseRequest(json: String): PlaceOrderRequest = placeOrderRequestFromJson(json)
  override fun parseResponse(json: String): PlaceOrderResponse = placeOrderResponseFromJson(json)
  override fun parseError(json: String): ErrorResponse = errorResponseFromJson(json)
}

@JsExport
abstract class JsAdvanceOrderStatusAPI<E : WebResponse<Any>> :
  OrderAPI.AdvanceOrderStatusAPI<Promise<E>>,
  SimpleAPI<AdvanceOrderStatusRequest, AdvanceOrderStatusResponse>() {
  override fun getPath(): String =
    OrderServiceConfig.SERVICE_PATH + OrderServiceConfig.ADVANCE_ORDER_STATUS_PATH

  override fun getPort(): Int = OrderServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.POST
  override fun getRequiredAuthRole(): AuthRole = AuthRole.CUSTOMER
  abstract override fun advanceOrderStatus(request: AdvanceOrderStatusRequest): Promise<E>

  override fun parseRequest(json: String): AdvanceOrderStatusRequest =
    advanceOrderStatusRequestFromJson(
      json,
    )

  override fun parseResponse(json: String): AdvanceOrderStatusResponse =
    advanceOrderStatusResponseFromJson(json)

  override fun parseError(json: String): ErrorResponse = errorResponseFromJson(json)
}

@JsExport
abstract class JsDiscardOrderAPI<E : WebResponse<Any>> :
  OrderAPI.DiscardOrderAPI<Promise<E>>,
  SimpleAPI<Nothing, DiscardOrderResponse>() {
  override fun getPath(): String =
    OrderServiceConfig.SERVICE_PATH + OrderServiceConfig.DISCARD_ORDER_PATH

  override fun getPort(): Int = OrderServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.DELETE
  override fun getRequiredAuthRole(): AuthRole = AuthRole.CUSTOMER
  abstract override fun discardOrder(id: String): Promise<E>

  override fun parseRequest(json: String): Nothing = throw UnsupportedOperationException()
  override fun parseResponse(json: String): DiscardOrderResponse =
    discardOrderResponseFromJson(json)

  override fun parseError(json: String): ErrorResponse = errorResponseFromJson(json)
}

@JsExport
abstract class JsUpdateOrderItemsAPI<E : WebResponse<Any>> :
  OrderAPI.UpdateOrderItemsAPI<Promise<E>>,
  SimpleAPI<UpdateOrderItemsRequest, UpdateOrderItemsResponse>() {
  override fun getPath(): String =
    OrderServiceConfig.SERVICE_PATH + OrderServiceConfig.UPDATE_ORDER_ITEMS_PATH

  override fun getPort(): Int = OrderServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.PATCH
  override fun getRequiredAuthRole(): AuthRole = AuthRole.CUSTOMER
  abstract override fun updateOrderItems(request: UpdateOrderItemsRequest): Promise<E>

  override fun parseRequest(json: String): UpdateOrderItemsRequest =
    updateOrderItemsRequestFromJson(
      json,
    )

  override fun parseResponse(json: String): UpdateOrderItemsResponse =
    updateOrderItemsResponseFromJson(json)

  override fun parseError(json: String): ErrorResponse = errorResponseFromJson(json)
}

@JsExport
abstract class JsUpdateDeliveryOrderInfoAPI<E : WebResponse<Any>> :
  OrderAPI.UpdateDeliveryOrderInfoAPI<Promise<E>>,
  SimpleAPI<UpdateDeliveryOrderRequest, UpdateDeliveryOrderResponse>() {
  override fun getPath(): String =
    OrderServiceConfig.SERVICE_PATH + OrderServiceConfig.UPDATE_DELIVERY_ORDER_INFO_PATH

  override fun getPort(): Int = OrderServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.PATCH
  override fun getRequiredAuthRole(): AuthRole = AuthRole.CUSTOMER
  abstract override fun updateDeliveryOrderInfo(request: UpdateDeliveryOrderRequest): Promise<E>

  override fun parseRequest(json: String): UpdateDeliveryOrderRequest =
    updateDeliveryOrderRequestFromJson(
      json,
    )

  override fun parseResponse(json: String): UpdateDeliveryOrderResponse =
    updateDeliveryOrderResponseFromJson(json)

  override fun parseError(json: String): ErrorResponse = errorResponseFromJson(json)
}

@JsExport
abstract class JsUpdateTakeawayOrderInfoAPI<E : WebResponse<Any>> :
  OrderAPI.UpdateTakeawayOrderInfoAPI<Promise<E>>,
  SimpleAPI<UpdateTakeawayOrderRequest, UpdateTakeawayOrderResponse>() {
  override fun getPath(): String =
    OrderServiceConfig.SERVICE_PATH + OrderServiceConfig.UPDATE_TAKEAWAY_ORDER_INFO_PATH

  override fun getPort(): Int = OrderServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.PATCH
  override fun getRequiredAuthRole(): AuthRole = AuthRole.CUSTOMER
  abstract override fun updateTakeawayOrderInfo(request: UpdateTakeawayOrderRequest): Promise<E>

  override fun parseRequest(json: String): UpdateTakeawayOrderRequest =
    updateTakeawayOrderRequestFromJson(
      json,
    )

  override fun parseResponse(json: String): UpdateTakeawayOrderResponse =
    updateTakeawayOrderResponseFromJson(json)

  override fun parseError(json: String): ErrorResponse = errorResponseFromJson(json)
}
