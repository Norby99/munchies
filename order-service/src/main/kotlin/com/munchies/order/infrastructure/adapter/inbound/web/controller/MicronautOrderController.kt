package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.order.application.port.inbound.*
import com.munchies.order.application.port.inbound.command.DiscardOrderCommand
import com.munchies.order.application.port.inbound.command.GetOrderDetailsCommand
import com.munchies.order.domain.model.OrderId
import com.munchies.order.infrastructure.adapter.dto.OrderDto
import com.munchies.order.infrastructure.adapter.dto.OrderItemDto
import com.munchies.order.infrastructure.adapter.dto.OrderType
import com.munchies.order.infrastructure.adapter.dto.factory.CommandFactory.toCommand
import com.munchies.order.infrastructure.adapter.inbound.OrderAPI.*
import com.munchies.order.infrastructure.adapter.inbound.request.*
import com.munchies.order.infrastructure.adapter.inbound.web.config.OrderServiceConfig
import com.munchies.order.infrastructure.adapter.inbound.web.config.OrderServices
import com.munchies.order.infrastructure.adapter.outbound.response.AdvanceOrderStatusResponse
import com.munchies.order.infrastructure.adapter.outbound.response.AdvanceOrderStatusResponseType
import com.munchies.order.infrastructure.adapter.outbound.response.DiscardOrderResponse
import com.munchies.order.infrastructure.adapter.outbound.response.DiscardOrderResponseType
import com.munchies.order.infrastructure.adapter.outbound.response.GetOrderDetailsResponse
import com.munchies.order.infrastructure.adapter.outbound.response.GetOrderDetailsResponseType
import com.munchies.order.infrastructure.adapter.outbound.response.PlaceOrderResponse
import com.munchies.order.infrastructure.adapter.outbound.response.PlaceOrderResponseType
import com.munchies.order.infrastructure.adapter.outbound.response.UpdateDeliveryOrderResponse
import com.munchies.order.infrastructure.adapter.outbound.response.UpdateDeliveryOrderResponseType
import com.munchies.order.infrastructure.adapter.outbound.response.UpdateOrderItemsResponse
import com.munchies.order.infrastructure.adapter.outbound.response.UpdateOrderItemsResponseType
import com.munchies.order.infrastructure.adapter.outbound.response.UpdateTakeawayOrderResponse
import com.munchies.order.infrastructure.adapter.outbound.response.UpdateTakeawayOrderResponseType
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.*
import io.micronaut.serde.annotation.SerdeImport
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.inject.Inject

/**
 * Micronaut HTTP controller for the order-service inbound web adapter.
 *
 * This controller exposes order-related HTTP endpoints and delegates business
 * operations to application inbound ports. It is responsible for:
 * - validating incoming request payloads at the HTTP boundary
 * - mapping DTOs to domain objects and commands
 * - translating use-case results into HTTP response codes and messages
 *
 * The controller intentionally avoids domain logic so the application and domain
 * layers remain independent from transport concerns.
 */
@SerdeImport(OrderDto::class)
@SerdeImport(OrderItemDto::class)
@SerdeImport(OrderType::class)
@SerdeImport(PlaceOrderRequest::class)
@SerdeImport(AdvanceOrderStatusRequest::class)
@SerdeImport(UpdateOrderItemsRequest::class)
@SerdeImport(UpdateDeliveryOrderRequest::class)
@SerdeImport(UpdateTakeawayOrderRequest::class)
@SerdeImport(GetOrderDetailsResponse::class)
@SerdeImport(GetOrderDetailsResponseType::class)
@SerdeImport(PlaceOrderResponse::class)
@SerdeImport(PlaceOrderResponseType::class)
@SerdeImport(AdvanceOrderStatusResponse::class)
@SerdeImport(AdvanceOrderStatusResponseType::class)
@SerdeImport(DiscardOrderResponse::class)
@SerdeImport(DiscardOrderResponseType::class)
@SerdeImport(UpdateOrderItemsResponse::class)
@SerdeImport(UpdateOrderItemsResponseType::class)
@SerdeImport(UpdateDeliveryOrderResponse::class)
@SerdeImport(UpdateDeliveryOrderResponseType::class)
@SerdeImport(UpdateTakeawayOrderResponse::class)
@SerdeImport(UpdateTakeawayOrderResponseType::class)
@Controller(
  value = OrderServiceConfig.SERVICE_PATH,
)
class MicronautOrderController(
  /**
   * Aggregated application services exposing order-related use cases.
   */
  @Inject
  private val services: OrderServices,
) :
  GetOrderDetailsAPI<String, HttpResponse<GetOrderDetailsResponse>>,
  PlaceOrderAPI<PlaceOrderRequest, HttpResponse<PlaceOrderResponse>>,
  AdvanceOrderStatusAPI<AdvanceOrderStatusRequest, HttpResponse<AdvanceOrderStatusResponse>>,
  DiscardOrderAPI<String, HttpResponse<DiscardOrderResponse>>,
  UpdateOrderItemsAPI<UpdateOrderItemsRequest, HttpResponse<UpdateOrderItemsResponse>>,
  UpdateDeliveryOrderInfoAPI<UpdateDeliveryOrderRequest, HttpResponse<UpdateDeliveryOrderResponse>>,
  UpdateTakeawayOrderInfoAPI<UpdateTakeawayOrderRequest, HttpResponse<UpdateTakeawayOrderResponse>> {

  private val getOrderDetails: GetOrderDetails = services.getOrderDetails
  private val placeOrder: PlaceOrder = services.placeOrder
  private val advanceOrderStatus: AdvanceOrderStatus = services.advanceOrderStatus
  private val discardOrder: DiscardOrder = services.discardOrder
  private val updateOrderItems: UpdateOrderItems = services.updateOrderItems
  private val updateDeliveryOrderInfo: UpdateDeliveryOrderInfo = services.updateDeliveryOrderInfo
  private val updateTakeawayOrderInfo: UpdateTakeawayOrderInfo = services.updateTakeawayOrderInfo

  /**
   * Handles `GET orders/{id}`.
   *
   * Translates the application-layer result into an HTTP response:
   * - `200 OK` with the order DTO if the order is found
   * - `404 Not Found` if the order does not exist
   *
   * @param id The order identifier received from the path.
   * @return An HTTP response containing the order DTO or a not-found status.
   */
  @Get(OrderServiceConfig.GET_ORDER_PATH)
  @Operation(
    summary = "Get order by id",
    description = "Retrieves an order by its unique identifier.",
  )
  @ApiResponse(responseCode = "200", description = "Found")
  @ApiResponse(responseCode = "404", description = "Not Found")
  override fun getOrderDetails(@PathVariable id: String): HttpResponse<GetOrderDetailsResponse> {
    return when (
      val res = getOrderDetails.execute(GetOrderDetailsCommand(OrderId(id)))
    ) {
      is GetOrderDetails.Result.Success -> HttpResponse.ok(
        GetOrderDetailsResponse(
          code = HttpStatus.OK.code,
          type = GetOrderDetailsResponseType.SUCCESS,
          //order = res.order
        )
      )
      is GetOrderDetails.Result.Failure.OrderNotFound -> HttpResponse.notFound(
        GetOrderDetailsResponse(
          code = HttpStatus.NOT_FOUND.code,
          type = GetOrderDetailsResponseType.ORDER_NOT_FOUND
        )
      )
    }
  }

  /**
   * Handles `POST orders/place`.
   *
   * Validates input data and delegates order placement to the application layer.
   * Supports three order types: Delivery, Takeaway, and DineIn.
   *
   * Response mapping:
   * - `200 OK` when order is placed successfully
   * - `400 Bad Request` when payload validation fails or date/items are invalid
   * - `500 Internal Server Error` when the use case reports a failure
   *
   * @param request Place order payload containing order type and details.
   * @return An HTTP response containing the created order DTO or an error status.
   */
  @Post(OrderServiceConfig.PLACE_ORDER_PATH)
  @Operation(
    summary = "Place a new order",
    description = "Places a new order with the provided order type and details.",
  )
  @ApiResponse(responseCode = "200", description = "Order placed successfully")
  @ApiResponse(responseCode = "400", description = "Invalid order data")
  @ApiResponse(responseCode = "500", description = "Failed to place order")
  override fun placeOrder(@Body request: PlaceOrderRequest): HttpResponse<PlaceOrderResponse> {
    return when (val res = placeOrder.execute(request.toCommand())) {
      is PlaceOrder.Result.Success -> HttpResponse.ok(
        PlaceOrderResponse(
          code = HttpStatus.OK.code,
          type = PlaceOrderResponseType.SUCCESS,
          //order = res.order
        )
      )
      is PlaceOrder.Result.Failure.InvalidDate -> HttpResponse.badRequest(
        PlaceOrderResponse(
          code = HttpStatus.BAD_REQUEST.code,
          type = PlaceOrderResponseType.INVALID_DATE,
        )
      )
      is PlaceOrder.Result.Failure.EmptyItems -> HttpResponse.badRequest(
        PlaceOrderResponse(
          code = HttpStatus.BAD_REQUEST.code,
          type = PlaceOrderResponseType.EMPTY_ITEMS,
        )
      )
      is PlaceOrder.Result.Failure.InvalidItemQuantity -> HttpResponse.badRequest(
        PlaceOrderResponse(
          code = HttpStatus.BAD_REQUEST.code,
          type = PlaceOrderResponseType.INVALID_ITEM_QUANTITY,
        )
      )
    }
  }

  /**
   * Handles `POST orders/{id}/advance`.
   *
   * Advances the order status to the next valid state.
   *
   * Response mapping:
   * - `200 OK` when status is advanced successfully
   * - `404 Not Found` when order does not exist
   * - `400 Bad Request` when state transition is invalid
   *
   * @param request Request containing the order identifier.
   * @return An HTTP response representing the operation outcome.
   */
  @Post(OrderServiceConfig.ADVANCE_ORDER_STATUS_PATH)
  @Operation(
    summary = "Advance order status",
    description = "Advances an order to the next status.",
  )
  @ApiResponse(responseCode = "200", description = "Order status advanced successfully")
  @ApiResponse(responseCode = "404", description = "Order not found")
  @ApiResponse(responseCode = "400", description = "Invalid status transition")
  override fun advanceOrderStatus(
    @Body request: AdvanceOrderStatusRequest
  ): HttpResponse<AdvanceOrderStatusResponse> {
    return when (
      advanceOrderStatus.execute(request.toCommand())
    ) {
      is AdvanceOrderStatus.Result.Success -> HttpResponse.ok(
        AdvanceOrderStatusResponse(
          code = HttpStatus.OK.code,
          type = AdvanceOrderStatusResponseType.SUCCESS
        )
      )
      is AdvanceOrderStatus.Result.Failure.OrderNotFound -> HttpResponse.notFound(
        AdvanceOrderStatusResponse(
          code = HttpStatus.NOT_FOUND.code,
          type = AdvanceOrderStatusResponseType.ORDER_NOT_FOUND
        )
      )
      is AdvanceOrderStatus.Result.Failure.InvalidTransition -> HttpResponse.badRequest(
        AdvanceOrderStatusResponse(
          code = HttpStatus.BAD_REQUEST.code,
          type = AdvanceOrderStatusResponseType.INVALID_TRANSACTION
        )
      )
    }
  }

  /**
   * Handles `Delete orders/{id}/discard`.
   *
   * Cancels/discards an order if it is still in a cancellable state.
   *
   * Response mapping:
   * - `200 OK` when order is discarded successfully
   * - `404 Not Found` when order does not exist
   * - `400 Bad Request` when order cannot be canceled
   *
   * @param id Request containing order identifiers.
   * @return An HTTP response representing the discard result.
   */
  @Delete(OrderServiceConfig.DISCARD_ORDER_PATH)
  @Operation(
    summary = "Discard/Cancel an order",
    description = "Cancels an order if it is still in a cancellable state.",
  )
  @ApiResponse(responseCode = "200", description = "Order discarded successfully")
  @ApiResponse(responseCode = "404", description = "Order not found")
  @ApiResponse(responseCode = "400", description = "Order cannot be cancelled")
  override fun discardOrder(@PathVariable id: String): HttpResponse<DiscardOrderResponse> {
    return when (
      discardOrder.execute(DiscardOrderCommand(OrderId(id)))
    ) {
      is DiscardOrder.Result.Success -> HttpResponse.ok(
        DiscardOrderResponse(
          code = HttpStatus.OK.code,
          type = DiscardOrderResponseType.SUCCESS
        )
      )
      is DiscardOrder.Result.Failure.OrderNotFound ->
        HttpResponse.notFound(
          DiscardOrderResponse(
            code = HttpStatus.NOT_FOUND.code,
            type = DiscardOrderResponseType.ORDER_NOT_FOUND
          )
        )
      is DiscardOrder.Result.Failure.OrderNotCancellable -> HttpResponse.badRequest(
        DiscardOrderResponse(
          code = HttpStatus.BAD_REQUEST.code,
          type = DiscardOrderResponseType.ORDER_NOT_CANCELLABLE
        )
      )
    }
  }

  /**
   * Handles `PATCH orders/{id}/items`.
   *
   * Updates the items in an order.
   *
   * Response mapping:
   * - `200 OK` when items are updated successfully
   * - `404 Not Found` when order does not exist
   * - `400 Bad Request` for unauthorized access or empty items
   *
   * @param request Request containing order identifier and new items.
   * @return An HTTP response representing the update result.
   */
  @Patch(OrderServiceConfig.UPDATE_ORDER_ITEMS_PATH)
  @Operation(
    summary = "Update order items",
    description = "Updates the items in an order.",
  )
  @ApiResponse(responseCode = "200", description = "Order items updated successfully")
  @ApiResponse(responseCode = "404", description = "Order not found")
  @ApiResponse(responseCode = "400", description = "Invalid items or unauthorized")
  override fun updateOrderItems(
    @Body request: UpdateOrderItemsRequest
  ): HttpResponse<UpdateOrderItemsResponse> {
    return when (
      updateOrderItems.execute(request.toCommand())
    ) {
      is UpdateOrderItems.Result.Success -> HttpResponse.ok(
        UpdateOrderItemsResponse(
          code = HttpStatus.OK.code,
          type = UpdateOrderItemsResponseType.SUCCESS
        )
      )
      is UpdateOrderItems.Result.Failure.OrderNotFound -> HttpResponse.notFound(
        UpdateOrderItemsResponse(
          code = HttpStatus.NOT_FOUND.code,
          type = UpdateOrderItemsResponseType.ORDER_NOT_FOUND
        )
      )
      is UpdateOrderItems.Result.Failure.Unauthorized -> HttpResponse.badRequest(
        UpdateOrderItemsResponse(
          code = HttpStatus.UNAUTHORIZED.code,
          type = UpdateOrderItemsResponseType.UNAUTHORIZED
        )
      )
      is UpdateOrderItems.Result.Failure.EmptyItems -> HttpResponse.badRequest(
        UpdateOrderItemsResponse(
          code = HttpStatus.BAD_REQUEST.code,
          type = UpdateOrderItemsResponseType.EMPTY_ITEMS
        )
      )
    }
  }

  /**
   * Handles `PATCH orders/{id}/delivery`.
   *
   * Updates delivery information for a delivery order.
   *
   * Response mapping:
   * - `200 OK` when delivery info is updated
   * - `404 Not Found` when order does not exist
   * - `400 Bad Request` for unauthorized access or invalid date
   *
   * @param request Request containing delivery order update details.
   * @return An HTTP response representing the update result.
   */
  @Patch(OrderServiceConfig.UPDATE_DELIVERY_ORDER_INFO_PATH)
  @Operation(
    summary = "Update delivery order info",
    description = "Updates delivery information for a delivery order.",
  )
  @ApiResponse(responseCode = "200", description = "Delivery info updated successfully")
  @ApiResponse(responseCode = "404", description = "Order not found")
  @ApiResponse(responseCode = "400", description = "Invalid data or unauthorized")
  override fun updateDeliveryOrderInfo(
    @Body request: UpdateDeliveryOrderRequest,
  ): HttpResponse<UpdateDeliveryOrderResponse> {
    return when (
      updateDeliveryOrderInfo.execute(request.toCommand())
    ) {
      is UpdateDeliveryOrderInfo.Result.Success -> HttpResponse.ok(
        UpdateDeliveryOrderResponse(
          code = HttpStatus.OK.code,
          type = UpdateDeliveryOrderResponseType.SUCCESS
        )
      )
      is UpdateDeliveryOrderInfo.Result.Failure.OrderNotFound -> HttpResponse.notFound(
        UpdateDeliveryOrderResponse(
          code = HttpStatus.NOT_FOUND.code,
          type = UpdateDeliveryOrderResponseType.ORDER_NOT_FOUND
        )
      )
      is UpdateDeliveryOrderInfo.Result.Failure.Unauthorized -> HttpResponse.badRequest(
        UpdateDeliveryOrderResponse(
          code = HttpStatus.UNAUTHORIZED.code,
          type = UpdateDeliveryOrderResponseType.UNAUTHORIZED
        )
      )
      is UpdateDeliveryOrderInfo.Result.Failure.InvalidDate ->
        HttpResponse.notFound(
          UpdateDeliveryOrderResponse(
            code = HttpStatus.NOT_FOUND.code,
            type = UpdateDeliveryOrderResponseType.INVALID_DATE
          )
        )
    }
  }

  /**
   * Handles `PATCH orders/{id}/takeaway`.
   *
   * Updates takeaway information for a takeaway order.
   *
   * Response mapping:
   * - `200 OK` when takeaway info is updated
   * - `404 Not Found` when order does not exist
   * - `400 Bad Request` for unauthorized access or invalid date
   *
   * @param request Request containing takeaway order update details.
   * @return An HTTP response representing the update result.
   */
  @Patch(OrderServiceConfig.UPDATE_TAKEAWAY_ORDER_INFO_PATH)
  @Operation(
    summary = "Update takeaway order info",
    description = "Updates pickup information for a takeaway order.",
  )
  @ApiResponse(responseCode = "200", description = "Takeaway info updated successfully")
  @ApiResponse(responseCode = "404", description = "Order not found")
  @ApiResponse(responseCode = "400", description = "Invalid data or unauthorized")
  override fun updateTakeawayOrderInfo(
    @Body request: UpdateTakeawayOrderRequest,
  ): HttpResponse<UpdateTakeawayOrderResponse> {
    return when (
      updateTakeawayOrderInfo.execute(request.toCommand())
    ) {
      is UpdateTakeawayOrderInfo.Result.Success -> HttpResponse.ok(
        UpdateTakeawayOrderResponse(
          code = HttpStatus.OK.code,
          type = UpdateTakeawayOrderResponseType.SUCCESS
        )
      )
      is UpdateTakeawayOrderInfo.Result.Failure.OrderNotFound -> HttpResponse.notFound(
        UpdateTakeawayOrderResponse(
          code = HttpStatus.NOT_FOUND.code,
          type = UpdateTakeawayOrderResponseType.ORDER_NOT_FOUND
        )
      )
      is UpdateTakeawayOrderInfo.Result.Failure.Unauthorized -> HttpResponse.badRequest(
        UpdateTakeawayOrderResponse(
          code = HttpStatus.UNAUTHORIZED.code,
          type = UpdateTakeawayOrderResponseType.UNAUTHORIZED
        )
      )
      is UpdateTakeawayOrderInfo.Result.Failure.InvalidDate -> HttpResponse.badRequest(
        UpdateTakeawayOrderResponse(
          code = HttpStatus.BAD_REQUEST.code,
          type = UpdateTakeawayOrderResponseType.INVALID_DATE
        )
      )
    }
  }
}
