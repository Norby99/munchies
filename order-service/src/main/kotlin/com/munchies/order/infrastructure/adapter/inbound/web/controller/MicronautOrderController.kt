package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.order.application.port.inbound.AdvanceOrderStatus
import com.munchies.order.application.port.inbound.DiscardOrder
import com.munchies.order.application.port.inbound.GetOrderDetails
import com.munchies.order.application.port.inbound.PlaceOrder
import com.munchies.order.application.port.inbound.UpdateDeliveryOrderInfo
import com.munchies.order.application.port.inbound.UpdateOrderItems
import com.munchies.order.application.port.inbound.UpdateTakeawayOrderInfo
import com.munchies.order.application.port.inbound.command.GetOrderDetailsCommand
import com.munchies.order.domain.model.OrderId
import com.munchies.order.infrastructure.adapter.dto.OrderDto
import com.munchies.order.infrastructure.adapter.dto.factory.CommandFactory.toCommand
import com.munchies.order.infrastructure.adapter.inbound.OrderAPI.*
import com.munchies.order.infrastructure.adapter.inbound.request.*
import com.munchies.order.infrastructure.adapter.inbound.web.config.OrderServiceConfig
import com.munchies.order.infrastructure.adapter.inbound.web.config.OrderServices
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Patch
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
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
@ExecuteOn(TaskExecutors.BLOCKING)
@SerdeImport(OrderDto::class)
@SerdeImport(OrderDto.Delivery::class)
@SerdeImport(OrderDto.Takeaway::class)
@SerdeImport(OrderDto.DineIn::class)
@SerdeImport(PlaceOrderRequest::class)
@SerdeImport(GetOrderDetailsRequest::class)
@SerdeImport(AdvanceOrderStatusRequest::class)
@SerdeImport(DiscardOrderRequest::class)
@SerdeImport(UpdateOrderItemsRequest::class)
@SerdeImport(UpdateDeliveryOrderRequest::class)
@SerdeImport(UpdateTakeawayOrderRequest::class)
@Controller(
  port = OrderServiceConfig.SERVICE_PORT.toString(),
  value = OrderServiceConfig.SERVICE_PATH,
)
class MicronautOrderController(
  /**
   * Aggregated application services exposing order-related use cases.
   */
  @Inject
  private val services: OrderServices,
) :
  GetOrderDetailsAPI<String, HttpResponse<OrderDto>>,
  PlaceOrderAPI<PlaceOrderRequest, HttpResponse<String>>,
  AdvanceOrderStatusAPI<AdvanceOrderStatusRequest, HttpResponse<String>>,
  DiscardOrderAPI<DiscardOrderRequest, HttpResponse<String>>,
  UpdateOrderItemsAPI<UpdateOrderItemsRequest, HttpResponse<String>>,
  UpdateDeliveryOrderInfoAPI<UpdateDeliveryOrderRequest, HttpResponse<String>>,
  UpdateTakeawayOrderInfoAPI<UpdateTakeawayOrderRequest, HttpResponse<String>> {

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
   * @param request The order identifier received from the path.
   * @return An HTTP response containing the order DTO or a not-found status.
   */
  @Get(OrderServiceConfig.GET_ORDER_PATH)
  @Operation(
    summary = "Get order by id",
    description = "Retrieves an order by its unique identifier.",
  )
  @ApiResponse(responseCode = "200", description = "Found")
  @ApiResponse(responseCode = "404", description = "Not Found")
  override fun getOrderDetails(@PathVariable id: String): HttpResponse<OrderDto> {
    return when (
      val res = getOrderDetails.execute(GetOrderDetailsCommand(OrderId(id)))
    ) {
      is GetOrderDetails.Result.Success -> HttpResponse.ok(res.order)
      is GetOrderDetails.Result.Failure.OrderNotFound -> HttpResponse.notFound()
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
  override fun placeOrder(@Body request: PlaceOrderRequest): HttpResponse<String> {
    return when (val res = placeOrder.execute(request.toCommand())) {
      is PlaceOrder.Result.Success ->
        HttpResponse.ok("Order placed successfully with ID: ${res.order.orderId}")
      is PlaceOrder.Result.Failure.InvalidDate ->
        HttpResponse.badRequest("Invalid date for order type")
      is PlaceOrder.Result.Failure.EmptyItems ->
        HttpResponse.badRequest("Order must contain at least one item")
      is PlaceOrder.Result.Failure.InvalidItemQuantity ->
        HttpResponse.badRequest("Item quantity must be greater than zero")
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
  override fun advanceOrderStatus(@Body request: AdvanceOrderStatusRequest): HttpResponse<String> {
    return when (
      advanceOrderStatus.execute(request.toCommand())
    ) {
      is AdvanceOrderStatus.Result.Success -> HttpResponse.ok("Order status advanced")
      is AdvanceOrderStatus.Result.Failure.OrderNotFound -> HttpResponse.notFound()
      is AdvanceOrderStatus.Result.Failure.InvalidTransition ->
        HttpResponse.badRequest("Invalid status transition")
    }
  }

  /**
   * Handles `POST orders/{id}/discard`.
   *
   * Cancels/discards an order if it is still in a cancellable state.
   *
   * Response mapping:
   * - `200 OK` when order is discarded successfully
   * - `404 Not Found` when order does not exist
   * - `400 Bad Request` when order cannot be canceled or unauthorized
   *
   * @param request Request containing order and customer identifiers.
   * @return An HTTP response representing the discard result.
   */
  @Post(OrderServiceConfig.DISCARD_ORDER_PATH)
  @Operation(
    summary = "Discard/Cancel an order",
    description = "Cancels an order if it is still in a cancellable state.",
  )
  @ApiResponse(responseCode = "200", description = "Order discarded successfully")
  @ApiResponse(responseCode = "404", description = "Order not found")
  @ApiResponse(responseCode = "400", description = "Order cannot be cancelled")
  override fun discardOrder(@Body request: DiscardOrderRequest): HttpResponse<String> {
    return when (
      discardOrder.execute(request.toCommand())
    ) {
      is DiscardOrder.Result.Success -> HttpResponse.ok("Order discarded")
      is DiscardOrder.Result.Failure.OrderNotFound -> HttpResponse.notFound()
      is DiscardOrder.Result.Failure.Unauthorized,
      is DiscardOrder.Result.Failure.OrderNotCancellable,
      ->
        HttpResponse.badRequest("Cannot discard this order")
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
  override fun updateOrderItems(@Body request: UpdateOrderItemsRequest): HttpResponse<String> {
    return when (
      updateOrderItems.execute(request.toCommand())
    ) {
      is UpdateOrderItems.Result.Success -> HttpResponse.ok("Order items updated")
      is UpdateOrderItems.Result.Failure.OrderNotFound -> HttpResponse.notFound()
      is UpdateOrderItems.Result.Failure.Unauthorized,
      is UpdateOrderItems.Result.Failure.EmptyItems,
      ->
        HttpResponse.badRequest("Cannot update items")
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
  ): HttpResponse<String> {
    return when (
      updateDeliveryOrderInfo.execute(request.toCommand())
    ) {
      is UpdateDeliveryOrderInfo.Result.Success -> HttpResponse.ok("Delivery info updated")
      is UpdateDeliveryOrderInfo.Result.Failure.OrderNotFound -> HttpResponse.notFound()
      is UpdateDeliveryOrderInfo.Result.Failure.Unauthorized,
      is UpdateDeliveryOrderInfo.Result.Failure.InvalidDate,
      ->
        HttpResponse.badRequest("Cannot update delivery info")
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
  ): HttpResponse<String> {
    return when (
      updateTakeawayOrderInfo.execute(request.toCommand())
    ) {
      is UpdateTakeawayOrderInfo.Result.Success -> HttpResponse.ok("Takeaway info updated")
      is UpdateTakeawayOrderInfo.Result.Failure.OrderNotFound -> HttpResponse.notFound()
      is UpdateTakeawayOrderInfo.Result.Failure.Unauthorized,
      is UpdateTakeawayOrderInfo.Result.Failure.InvalidDate,
      ->
        HttpResponse.badRequest("Cannot update takeaway info")
    }
  }
}
