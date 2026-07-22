package com.munchies.order.infrastructure.adapter.dto.factory

import com.munchies.order.application.port.inbound.command.*
import com.munchies.order.domain.model.CustomerId
import com.munchies.order.domain.model.OrderId
import com.munchies.order.domain.model.RestaurantId
import com.munchies.order.infrastructure.adapter.dto.OrderType.*
import com.munchies.order.infrastructure.adapter.dto.factory.OrderItemDtoFactory.toDomain
import com.munchies.order.infrastructure.adapter.inbound.request.*

/**
 * Factory object to convert inbound request DTOs into domain command objects.
 */
object CommandFactory {

  /**
   * Converts a [PlaceOrderRequest] into a corresponding [PlaceOrderCommand].
   * The conversion is based on the order type specified in the request.
   *
   * @throws IllegalArgumentException if required fields for the specific order type are missing.
   */
  fun PlaceOrderRequest.toCommand(): PlaceOrderCommand = when (this.orderType) {
    DELIVERY -> PlaceOrderCommand.Delivery(
      restaurantId = RestaurantId(restaurantId),
      customerId = CustomerId(customerId),
      items = items.map { it.toDomain() },
      estimatedDeliveryTime = estimatedDeliveryTime
        ?: throw IllegalArgumentException(
          "Estimated delivery time is required for delivery orders",
        ),
      deliveryAddress = deliveryAddress
        ?: throw IllegalArgumentException("Delivery address is required for delivery orders"),
      bellName = bellName
        ?: throw IllegalArgumentException("Bell name is required for delivery orders"),
      customerPhone = customerPhone
        ?: throw IllegalArgumentException("Customer phone is required for delivery orders"),
    )
    TAKEAWAY -> PlaceOrderCommand.Takeaway(
      restaurantId = RestaurantId(restaurantId),
      customerId = CustomerId(customerId),
      items = items.map { it.toDomain() },
      pickupTime = pickupTime
        ?: throw IllegalArgumentException("Pickup time is required for takeaway orders"),
      customerName = customerName
        ?: throw IllegalArgumentException("Customer name is required for takeaway orders"),
    )
    DINE_IN -> PlaceOrderCommand.DineIn(
      restaurantId = RestaurantId(restaurantId),
      customerId = CustomerId(customerId),
      items = items.map { it.toDomain() },
      tableNumber = tableNumber
        ?: throw IllegalArgumentException("Table number is required for dine-in orders"),
      numberOfGuests = numberOfGuests
        ?: throw IllegalArgumentException("Number of guests is required for dine-in orders"),
    )
  }

  /**
   * Converts an [AdvanceOrderStatusRequest] into a corresponding [AdvanceOrderStatusCommand].
   */
  fun AdvanceOrderStatusRequest.toCommand(): AdvanceOrderStatusCommand =
    AdvanceOrderStatusCommand(OrderId(orderId))

  /**
   * Converts an [UpdateOrderItemsRequest] into a corresponding [UpdateOrderItemsCommand].
   */
  fun UpdateOrderItemsRequest.toCommand(): UpdateOrderItemsCommand = UpdateOrderItemsCommand(
    orderId = OrderId(orderId),
    customerId = CustomerId(customerId),
    items = items.map { it.toDomain() },
  )

  /**
   * Converts an [UpdateDeliveryOrderRequest] into a corresponding [UpdateDeliveryOrderCommand].
   */
  fun UpdateDeliveryOrderRequest.toCommand(): UpdateDeliveryOrderCommand =
    UpdateDeliveryOrderCommand(
      orderId = OrderId(orderId),
      customerId = CustomerId(customerId),
      estimatedDeliveryTime = estimatedDeliveryTime,
      deliveryAddress = deliveryAddress,
      bellName = bellName,
      customerPhone = customerPhone,
    )

  /**
   * Converts an [UpdateTakeawayOrderRequest] into a corresponding [UpdateTakeawayOrderCommand].
   */
  fun UpdateTakeawayOrderRequest.toCommand(): UpdateTakeawayOrderCommand =
    UpdateTakeawayOrderCommand(
      orderId = OrderId(orderId),
      customerId = CustomerId(customerId),
      pickupTime = pickupTime,
      customerName = customerName,
    )
}
