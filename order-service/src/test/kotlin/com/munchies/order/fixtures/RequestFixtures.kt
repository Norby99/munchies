package com.munchies.order.fixtures

import com.munchies.order.domain.model.DeliveryOrder
import com.munchies.order.domain.model.OrderId
import com.munchies.order.domain.model.TakeawayOrder
import com.munchies.order.infrastructure.adapter.dto.OrderItemDto
import com.munchies.order.infrastructure.adapter.dto.OrderType
import com.munchies.order.infrastructure.adapter.inbound.request.AdvanceOrderStatusRequest
import com.munchies.order.infrastructure.adapter.inbound.request.DiscardOrderRequest
import com.munchies.order.infrastructure.adapter.inbound.request.PlaceOrderRequest
import com.munchies.order.infrastructure.adapter.inbound.request.UpdateDeliveryOrderRequest
import com.munchies.order.infrastructure.adapter.inbound.request.UpdateOrderItemsRequest
import com.munchies.order.infrastructure.adapter.inbound.request.UpdateTakeawayOrderRequest

/** Creates a PlaceOrderRequest.Delivery object from a DeliveryOrder.
 * @param order The DeliveryOrder to convert to a PlaceOrderRequest.
 * @return A PlaceOrderRequest.Delivery object with the same data as the DeliveryOrder.
 */
fun createPlaceOrderRequest(order: DeliveryOrder = createDeliveryOrder()) = PlaceOrderRequest(
  restaurantId = order.restaurantId.value,
  customerId = order.customerId.value,
  items = order.items.map {
    OrderItemDto(
      menuItemId = it.menuItemId.value,
      quantity = it.quantity,
    )
  },
  orderType = OrderType.DELIVERY,
  estimatedDeliveryTime = order.deliveryInfo.estimatedDeliveryTime,
  deliveryAddress = order.deliveryInfo.deliveryAddress,
  bellName = order.deliveryInfo.bellName,
  customerPhone = order.deliveryInfo.customerPhone,
)

/** Creates a DiscardOrderRequest from an OrderId.
 * @param orderId The OrderId of the order to discard. Defaults to defaultOrderId.
 * @return A DiscardOrderRequest with the given OrderId and defaultCustomerId.
 */
fun createDiscardOrderRequest(orderId: OrderId = defaultOrderId) = DiscardOrderRequest(
  orderId = orderId.value,
  customerId = defaultCustomerId.value,
)

fun createUpdateTakeawayOrderRequest(order: TakeawayOrder = createTakeawayOrder()) =
  UpdateTakeawayOrderRequest(
    orderId = order.id.value,
    customerId = order.customerId.value,
    pickupTime = order.takeawayInfo.pickupTime,
    customerName = order.takeawayInfo.customerName,
  )

fun createUpdateDeliveryOrderRequest(order: DeliveryOrder = createDeliveryOrder()) =
  UpdateDeliveryOrderRequest(
    orderId = order.id.value,
    customerId = order.customerId.value,
    estimatedDeliveryTime = order.deliveryInfo.estimatedDeliveryTime,
    deliveryAddress = order.deliveryInfo.deliveryAddress,
    bellName = order.deliveryInfo.bellName,
    customerPhone = order.deliveryInfo.customerPhone,
  )

fun createUpdateOrderItemsRequest(
  order: DeliveryOrder = createDeliveryOrder(),
  items: List<OrderItemDto> = createItemsDto(),
) = UpdateOrderItemsRequest(
  orderId = order.id.value,
  customerId = order.customerId.value,
  items = items,
)

/**
 *
 */
fun createAdvanceOrderStatusRequest(orderId: OrderId = defaultOrderId) = AdvanceOrderStatusRequest(
  orderId = orderId.value,
)
