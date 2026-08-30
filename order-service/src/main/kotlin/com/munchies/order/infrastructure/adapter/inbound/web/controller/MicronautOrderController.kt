package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.commons.domain.port.ValidationException
import com.munchies.commons.infrastructure.adapter.ErrorResponse
import com.munchies.order.application.port.inbound.*
import com.munchies.order.application.port.inbound.command.DiscardOrderCommand
import com.munchies.order.application.port.inbound.command.GetOrderDetailsCommand
import com.munchies.order.application.port.inbound.command.GetOrdersCommand
import com.munchies.order.domain.model.CustomerId
import com.munchies.order.domain.model.OrderId
import com.munchies.order.domain.model.OrderStatus
import com.munchies.order.domain.model.RestaurantId
import com.munchies.order.infrastructure.adapter.dto.OrderDto
import com.munchies.order.infrastructure.adapter.dto.OrderItemDto
import com.munchies.order.infrastructure.adapter.dto.OrderType
import com.munchies.order.infrastructure.adapter.dto.factory.CommandFactory.toCommand
import com.munchies.order.infrastructure.adapter.inbound.OrderAPI
import com.munchies.order.infrastructure.adapter.inbound.request.*
import com.munchies.order.infrastructure.adapter.inbound.web.config.OrderServiceConfig
import com.munchies.order.infrastructure.adapter.inbound.web.config.OrderServices
import com.munchies.order.infrastructure.adapter.inbound.web.controller.exception.NotFoundException
import com.munchies.order.infrastructure.adapter.inbound.web.controller.exception.UnauthorizedException
import com.munchies.order.infrastructure.adapter.inbound.web.controller.exception.UnexpectedException
import com.munchies.order.infrastructure.adapter.outbound.response.*
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
 * - throwing domain/http exceptions handled by ExceptionHandlers
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
@SerdeImport(PlaceOrderResponse::class)
@SerdeImport(AdvanceOrderStatusResponse::class)
@SerdeImport(DiscardOrderResponse::class)
@SerdeImport(UpdateOrderItemsResponse::class)
@SerdeImport(UpdateDeliveryOrderResponse::class)
@SerdeImport(UpdateTakeawayOrderResponse::class)
@SerdeImport(ErrorResponse::class)
@SerdeImport(NotFoundException::class)
@SerdeImport(UnauthorizedException::class)
@SerdeImport(UnexpectedException::class)
@SerdeImport(ValidationException::class)
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
  OrderAPI.GetOrderDetailsAPI<HttpResponse<GetOrderDetailsResponse>>,
  OrderAPI.GetOrdersAPI<HttpResponse<GetOrdersResponse>>,
  OrderAPI.PlaceOrderAPI<HttpResponse<PlaceOrderResponse>>,
  OrderAPI.AdvanceOrderStatusAPI<HttpResponse<AdvanceOrderStatusResponse>>,
  OrderAPI.DiscardOrderAPI<HttpResponse<DiscardOrderResponse>>,
  OrderAPI.UpdateOrderItemsAPI<HttpResponse<UpdateOrderItemsResponse>>,
  OrderAPI.UpdateDeliveryOrderInfoAPI<HttpResponse<UpdateDeliveryOrderResponse>>,
  OrderAPI.UpdateTakeawayOrderInfoAPI<HttpResponse<UpdateTakeawayOrderResponse>> {

  private val getOrderDetails: GetOrderDetails = services.getOrderDetails
  private val getOrders: GetOrders = services.getOrders
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
  @ApiResponse(responseCode = "404", description = "Order not found")
  override fun getOrderDetails(@PathVariable id: String): HttpResponse<GetOrderDetailsResponse> {
    return when (
      val res = getOrderDetails.execute(GetOrderDetailsCommand(OrderId(id)))
    ) {
      is GetOrderDetails.Result.Success -> HttpResponse.ok(
        GetOrderDetailsResponse(
          result = res.order,
          code = HttpStatus.OK.code,
        ),
      )
      is GetOrderDetails.Result.Failure.OrderNotFound -> throw NotFoundException("Order not found")
    }
  }

  /**
   * Handles `GET orders` with optional query parameters for filtering.
   *
   * Translates the application-layer result into an HTTP response:
   * - `200 OK` with a list of order DTOs if orders are found
   * - `200 OK` with an empty list if no orders match the filters
   *
   * @param restaurantId Optional filter for restaurant identifier.
   * @param customerId Optional filter for customer identifier.
   * @param orderStatus Optional filter for order status.
   * @return An HTTP response containing a list of order DTOs or an empty list.
   */
  @Get(OrderServiceConfig.GET_ORDERS_PATH)
  @Operation(
    summary = "Get orders with optional filters",
    description = "Retrieves all orders filtered by customerId, restaurantId and order status.",
  )
  @ApiResponse(responseCode = "200", description = "Orders found")
  override fun getOrders(
    @QueryValue("restaurantId") restaurantId: String?,
    @QueryValue("customerId") customerId: String?,
    @QueryValue("status") orderStatus: String?,
  ): HttpResponse<GetOrdersResponse> {
    val command = GetOrdersCommand(
      restaurantId = restaurantId?.let { RestaurantId(it) },
      customerId = customerId?.let { CustomerId(it) },
      orderStatus = orderStatus?.let { OrderStatus.valueOf(it) },
    )

    return when (val res = getOrders.execute(command)) {
      is GetOrders.Result.Success -> HttpResponse.ok(
        GetOrdersResponse(
          result = res.orders,
          code = HttpStatus.OK.code,
        ),
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
  override fun placeOrder(@Body request: PlaceOrderRequest): HttpResponse<PlaceOrderResponse> {
    return when (val res = placeOrder.execute(request.toCommand())) {
      is PlaceOrder.Result.Success -> HttpResponse.ok(
        PlaceOrderResponse(
          result = res.order,
          code = HttpStatus.OK.code,
        ),
      )
      is PlaceOrder.Result.Failure.InvalidDate -> throw ValidationException("Invalid date")
      is PlaceOrder.Result.Failure.EmptyItems -> throw ValidationException("Empty items")
      is PlaceOrder.Result.Failure.InvalidItemQuantity -> throw ValidationException(
        "Invalid item quantity",
      )
    }
  }

  /**
   * Handles `POST orders/advance`.
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
    @Body request: AdvanceOrderStatusRequest,
  ): HttpResponse<AdvanceOrderStatusResponse> {
    return when (
      advanceOrderStatus.execute(request.toCommand())
    ) {
      is AdvanceOrderStatus.Result.Success -> HttpResponse.ok(
        AdvanceOrderStatusResponse(
          code = HttpStatus.OK.code,
        ),
      )
      is AdvanceOrderStatus.Result.Failure.OrderNotFound -> throw NotFoundException(
        "Order not found",
      )
      is AdvanceOrderStatus.Result.Failure.InvalidTransition -> throw ValidationException(
        "Invalid status transition",
      )
    }
  }

  /**
   * Handles `DELETE orders/{id}`.
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
        ),
      )
      is DiscardOrder.Result.Failure.OrderNotFound -> throw NotFoundException("Order not found")
      is DiscardOrder.Result.Failure.OrderNotCancellable -> throw ValidationException(
        "Order cannot be cancelled",
      )
    }
  }

  /**
   * Handles `PATCH orders/items`.
   *
   * Updates the items in an order.
   *
   * Response mapping:
   * - `200 OK` when items are updated successfully
   * - `404 Not Found` when order does not exist
   * - `401 Unauthorized` for unauthorized access
   * - `400 Bad Request` for empty items
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
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @ApiResponse(responseCode = "400", description = "Empty items")
  override fun updateOrderItems(
    @Body request: UpdateOrderItemsRequest,
  ): HttpResponse<UpdateOrderItemsResponse> {
    return when (
      updateOrderItems.execute(request.toCommand())
    ) {
      is UpdateOrderItems.Result.Success -> HttpResponse.ok(
        UpdateOrderItemsResponse(
          code = HttpStatus.OK.code,
        ),
      )
      is UpdateOrderItems.Result.Failure.OrderNotFound -> throw NotFoundException("Order not found")
      is UpdateOrderItems.Result.Failure.Unauthorized -> throw UnauthorizedException("Unauthorized")
      is UpdateOrderItems.Result.Failure.EmptyItems -> throw ValidationException("Empty items")
    }
  }

  /**
   * Handles `PATCH orders/delivery`.
   *
   * Updates delivery information for a delivery order.
   *
   * Response mapping:
   * - `200 OK` when delivery info is updated
   * - `404 Not Found` when order does not exist
   * - `401 Unauthorized` for unauthorized access
   * - `400 Bad Request` for invalid date
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
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @ApiResponse(responseCode = "400", description = "Invalid date")
  override fun updateDeliveryOrderInfo(
    @Body request: UpdateDeliveryOrderRequest,
  ): HttpResponse<UpdateDeliveryOrderResponse> {
    return when (
      updateDeliveryOrderInfo.execute(request.toCommand())
    ) {
      is UpdateDeliveryOrderInfo.Result.Success -> HttpResponse.ok(
        UpdateDeliveryOrderResponse(
          code = HttpStatus.OK.code,
        ),
      )
      is UpdateDeliveryOrderInfo.Result.Failure.OrderNotFound -> throw NotFoundException(
        "Order not found",
      )
      is UpdateDeliveryOrderInfo.Result.Failure.Unauthorized -> throw UnauthorizedException(
        "Unauthorized",
      )
      is UpdateDeliveryOrderInfo.Result.Failure.InvalidDate -> throw ValidationException(
        "Invalid date",
      )
    }
  }

  /**
   * Handles `PATCH orders/takeaway`.
   *
   * Updates takeaway information for a takeaway order.
   *
   * Response mapping:
   * - `200 OK` when takeaway info is updated
   * - `404 Not Found` when order does not exist
   * - `401 Unauthorized` for unauthorized access
   * - `400 Bad Request` for invalid date
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
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @ApiResponse(responseCode = "400", description = "Invalid date")
  override fun updateTakeawayOrderInfo(
    @Body request: UpdateTakeawayOrderRequest,
  ): HttpResponse<UpdateTakeawayOrderResponse> {
    return when (
      updateTakeawayOrderInfo.execute(request.toCommand())
    ) {
      is UpdateTakeawayOrderInfo.Result.Success -> HttpResponse.ok(
        UpdateTakeawayOrderResponse(
          code = HttpStatus.OK.code,
        ),
      )
      is UpdateTakeawayOrderInfo.Result.Failure.OrderNotFound -> throw NotFoundException(
        "Order not found",
      )
      is UpdateTakeawayOrderInfo.Result.Failure.Unauthorized -> throw UnauthorizedException(
        "Unauthorized",
      )
      is UpdateTakeawayOrderInfo.Result.Failure.InvalidDate -> throw ValidationException(
        "Invalid date",
      )
    }
  }
}
