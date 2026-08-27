package com.munchies.e2e.fixtures.order

import com.munchies.order.infrastructure.adapter.dto.OrderItemDto
import com.munchies.order.infrastructure.adapter.dto.OrderType
import com.munchies.order.infrastructure.adapter.inbound.request.PlaceOrderRequest

object OrderFixtures {
  val deliveryOrderJson = PlaceOrderRequest(
    restaurantId = "rest-munchies-99",
    customerId = "cust-jack-01",
    items = listOf(OrderItemDto(menuItemId = "burger-super-double", quantity = 1)),
    orderType = OrderType.DELIVERY,
    estimatedDeliveryTime = "1814380800000",
    deliveryAddress = "Via Strombi 67",
    bellName = "Antonio",
    customerPhone = "+39333998877",
  ).toJson()
}
