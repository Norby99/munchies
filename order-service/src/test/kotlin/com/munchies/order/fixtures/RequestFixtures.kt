package com.munchies.order.fixtures

import com.munchies.order.domain.model.DeliveryOrder
import com.munchies.order.infrastructure.adapter.dto.OrderItemDto
import com.munchies.order.infrastructure.adapter.inbound.request.DeliveryRequest

/** Creates a PlaceOrderRequest.Delivery object from a DeliveryOrder.
 * @param order The DeliveryOrder to convert to a PlaceOrderRequest.Delivery.
 * @return A PlaceOrderRequest.Delivery object with the same data as the DeliveryOrder.
 */
fun createPlaceOrderRequest(order: DeliveryOrder = createDeliveryOrder()) = DeliveryRequest(
  restaurantId = order.restaurantId.value,
  customerId = order.customerId.value,
  items = order.items.map {
    OrderItemDto(
      menuItemId = it.menuItemId.value,
      quantity = it.quantity,
    )
  },
  estimatedDeliveryTime = order.deliveryInfo.estimatedDeliveryTime,
  deliveryAddress = order.deliveryInfo.deliveryAddress,
  bellName = order.deliveryInfo.bellName,
  customerPhone = order.deliveryInfo.customerPhone,
)
