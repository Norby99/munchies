package com.munchies.order.infrastructure.adapter.dto.factory

import com.munchies.order.application.port.inbound.command.*
import com.munchies.order.domain.model.CustomerId
import com.munchies.order.domain.model.OrderId
import com.munchies.order.domain.model.RestaurantId
import com.munchies.order.infrastructure.adapter.dto.factory.OrderItemDTOFactory.toDomain
import com.munchies.order.infrastructure.adapter.inbound.request.*

object CommandFactory {

  fun PlaceOrderRequest.toCommand(): PlaceOrderCommand = when (this) {
    is DeliveryRequest -> PlaceOrderCommand.Delivery(
      restaurantId = RestaurantId(restaurantId),
      customerId = CustomerId(customerId),
      items = items.map { it.toDomain() },
      estimatedDeliveryTime = estimatedDeliveryTime,
      deliveryAddress = deliveryAddress,
      bellName = bellName,
      customerPhone = customerPhone,
    )
    is TakeawayRequest -> PlaceOrderCommand.Takeaway(
      restaurantId = RestaurantId(restaurantId),
      customerId = CustomerId(customerId),
      items = items.map { it.toDomain() },
      pickupTime = pickupTime,
      customerName = customerName,
    )
    is DineInRequest -> PlaceOrderCommand.DineIn(
      restaurantId = RestaurantId(restaurantId),
      customerId = CustomerId(customerId),
      items = items.map { it.toDomain() },
      tableNumber = tableNumber,
      numberOfGuests = numberOfGuests,
    )
    else -> throw IllegalArgumentException(
      "Unknown PlaceOrderRequest type: ${this::class.simpleName}",
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
