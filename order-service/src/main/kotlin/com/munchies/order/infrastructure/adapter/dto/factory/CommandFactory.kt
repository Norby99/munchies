package com.munchies.order.infrastructure.adapter.dto.factory

import com.munchies.order.application.port.inbound.command.*
import com.munchies.order.domain.model.CustomerId
import com.munchies.order.domain.model.OrderId
import com.munchies.order.domain.model.RestaurantId
import com.munchies.order.infrastructure.adapter.dto.OrderType.*
import com.munchies.order.infrastructure.adapter.dto.factory.OrderItemDtoFactory.toDomain
import com.munchies.order.infrastructure.adapter.inbound.request.*

object CommandFactory {

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

  fun AdvanceOrderStatusRequest.toCommand(): AdvanceOrderStatusCommand =
    AdvanceOrderStatusCommand(OrderId(orderId))

  fun DiscardOrderRequest.toCommand(): DiscardOrderCommand =
    DiscardOrderCommand(OrderId(orderId), CustomerId(customerId))

  fun UpdateOrderItemsRequest.toCommand(): UpdateOrderItemsCommand = UpdateOrderItemsCommand(
    orderId = OrderId(orderId),
    customerId = CustomerId(customerId),
    items = items.map { it.toDomain() },
  )

  fun UpdateDeliveryOrderRequest.toCommand(): UpdateDeliveryOrderCommand =
    UpdateDeliveryOrderCommand(
      orderId = OrderId(orderId),
      customerId = CustomerId(customerId),
      estimatedDeliveryTime = estimatedDeliveryTime,
      deliveryAddress = deliveryAddress,
      bellName = bellName,
      customerPhone = customerPhone,
    )

  fun UpdateTakeawayOrderRequest.toCommand(): UpdateTakeawayOrderCommand =
    UpdateTakeawayOrderCommand(
      orderId = OrderId(orderId),
      customerId = CustomerId(customerId),
      pickupTime = pickupTime,
      customerName = customerName,
    )
}
