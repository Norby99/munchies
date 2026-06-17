package com.munchies.order.infrastructure.adapter.dto.factory

import com.munchies.order.application.port.inbound.command.PlaceOrderCommand
import com.munchies.order.domain.model.CustomerId
import com.munchies.order.domain.model.RestaurantId
import com.munchies.order.infrastructure.adapter.dto.factory.OrderItemDTOFactory.toDomain
import com.munchies.order.infrastructure.adapter.inbound.request.PlaceOrderRequest

object CommandFactory {

  fun PlaceOrderRequest.toCommand(): PlaceOrderCommand {
    return when (this) {
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
  }
}
