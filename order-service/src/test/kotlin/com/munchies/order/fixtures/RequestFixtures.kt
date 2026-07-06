package com.munchies.order.fixtures

import com.munchies.order.domain.model.DeliveryOrder
import com.munchies.order.domain.model.OrderId
import com.munchies.order.infrastructure.adapter.dto.OrderItemDto
import com.munchies.order.infrastructure.adapter.dto.OrderType
import com.munchies.order.infrastructure.adapter.inbound.request.AdvanceOrderStatusRequest
import com.munchies.order.infrastructure.adapter.inbound.request.DiscardOrderRequest
import com.munchies.order.infrastructure.adapter.inbound.request.PlaceOrderRequest
import com.munchies.order.infrastructure.adapter.inbound.request.UpdateDeliveryOrderRequest
import com.munchies.order.infrastructure.adapter.inbound.request.UpdateOrderItemsRequest
import com.munchies.order.infrastructure.adapter.inbound.request.UpdateTakeawayOrderRequest
import java.time.Instant

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

fun createUpdateTakeawayOrderRequest(
  orderId: OrderId = defaultOrderId,
  pickupTime: Long = Instant.now().plusSeconds(3600).toEpochMilli(),
  customerName: String = "John Doe",
) = UpdateTakeawayOrderRequest(
  orderId = orderId.value,
  customerId = defaultCustomerId.value,
  pickupTime = pickupTime,
  customerName = customerName,
)

fun createUpdateDeliveryOrderRequest(
  orderId: OrderId = defaultOrderId,
  estimatedDeliveryTime: Long = Instant.now().plusSeconds(3600).toEpochMilli(),
  deliveryAddress: String = "Via Roma 1, Milano",
  bellName: String = "Rossi",
  customerPhone: String = "1234567890",
) = UpdateDeliveryOrderRequest(
  orderId = orderId.value,
  customerId = defaultCustomerId.value,
  estimatedDeliveryTime = estimatedDeliveryTime,
  deliveryAddress = deliveryAddress,
  bellName = bellName,
  customerPhone = customerPhone,
)

fun createUpdateOrderItemsRequest(
  orderId: OrderId = defaultOrderId,
  items: List<OrderItemDto> = createItemsDto(),
) = UpdateOrderItemsRequest(
  orderId = orderId.value,
  customerId = defaultCustomerId.value,
  items = items,
)

/**
 *
 */
fun createAdvanceOrderStatusRequest(orderId: OrderId = defaultOrderId) = AdvanceOrderStatusRequest(
  orderId = orderId.value,
)
