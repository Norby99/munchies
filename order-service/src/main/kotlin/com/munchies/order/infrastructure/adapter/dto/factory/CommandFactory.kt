package com.munchies.order.infrastructure.adapter.dto.factory

import com.munchies.order.application.port.inbound.command.*
import com.munchies.order.domain.model.CustomerId
import com.munchies.order.domain.model.OrderId
import com.munchies.order.domain.model.RestaurantId
import com.munchies.order.infrastructure.adapter.dto.factory.OrderItemDTOFactory.toDomain
import com.munchies.order.infrastructure.adapter.inbound.request.*

object CommandFactory {

  fun PlaceOrderRequest.toCommand(): PlaceOrderCommand = when (this) {
    is PlaceOrderRequest.Delivery -> PlaceOrderCommand.Delivery(
      restaurantId = RestaurantId(restaurantId),
      customerId = CustomerId(customerId),
      items = items.map { it.toDomain() },
      estimatedDeliveryTime = estimatedDeliveryTime,
      deliveryAddress = deliveryAddress,
      bellName = bellName,
      customerPhone = customerPhone,
    )
    is PlaceOrderRequest.Takeaway -> PlaceOrderCommand.Takeaway(
      restaurantId = RestaurantId(restaurantId),
      customerId = CustomerId(customerId),
      items = items.map { it.toDomain() },
      pickupTime = pickupTime,
      customerName = customerName,
    )
    is PlaceOrderRequest.DineIn -> PlaceOrderCommand.DineIn(
      restaurantId = RestaurantId(restaurantId),
      customerId = CustomerId(customerId),
      items = items.map { it.toDomain() },
      tableNumber = tableNumber,
      numberOfGuests = numberOfGuests,
    )
  }

  fun AdvanceOrderStatusRequest.toCommand(): AdvanceOrderStatusCommand =
    AdvanceOrderStatusCommand(OrderId(orderId))

  fun DiscardOrderRequest.toCommand(): DiscardOrderCommand =
    DiscardOrderCommand(OrderId(orderId), CustomerId(customerId))

  fun UpdateOrderItemsRequest.toCommand(): UpdateOrderItemsCommand = UpdateOrderItemsCommand(
    OrderId(orderId),
    CustomerId(customerId),
    items.map { it.toDomain() },
  )

  fun UpdateDeliveryOrderRequest.toCommand(): UpdateDeliveryOrderCommand =
    UpdateDeliveryOrderCommand(
      OrderId(orderId),
      CustomerId(customerId),
      estimatedDeliveryTime,
      deliveryAddress,
      bellName,
      customerPhone,
    )

  fun UpdateTakeawayOrderRequest.toCommand(): UpdateTakeawayOrderCommand =
    UpdateTakeawayOrderCommand(
      OrderId(orderId),
      CustomerId(customerId),
      pickupTime,
      customerName,
    )
}
