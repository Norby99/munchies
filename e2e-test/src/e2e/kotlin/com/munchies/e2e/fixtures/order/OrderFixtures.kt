package com.munchies.e2e.fixtures.order

import com.munchies.order.infrastructure.adapter.dto.OrderItemDto
import com.munchies.order.infrastructure.adapter.dto.OrderType
import com.munchies.order.infrastructure.adapter.inbound.request.PlaceOrderRequest
import kotlin.time.Duration.Companion.days

object OrderFixtures {
  /**
   * A delivery order for one dish of a real restaurant: order-service prices every item from the
   * restaurant's menu (through restaurant-service), so both ids must exist. The delivery time is
   * one day ahead, since order-service refuses times in the past.
   */
  fun deliveryOrderJson(restaurantId: String, menuItemId: String): String = PlaceOrderRequest(
    restaurantId = restaurantId,
    customerId = "",
    items = listOf(OrderItemDto(menuItemId = menuItemId, quantity = 1)),
    orderType = OrderType.DELIVERY,
    estimatedDeliveryTime = (System.currentTimeMillis() + 1.days.inWholeMilliseconds).toString(),
    deliveryAddress = "Via Strombi 67",
    bellName = "Antonio",
    customerPhone = "+39333998877",
  ).toJson()
}
