package com.munchies.order.fixtures

import com.munchies.order.domain.model.CustomerId
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
fun createPlaceOrderRequest(order: DeliveryOrder = createDeliveryOrder()) =
  createTestPlaceOrderRequest(
    orderType = OrderType.DELIVERY,
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

/** Creates a PlaceOrderRequest.
 * @param orderType The type of the order (DELIVERY, TAKEAWAY, DINE_IN).
 * @param restaurantId The ID of the restaurant. Defaults to defaultRestaurantId.
 * @param customerId The ID of the customer. Defaults to defaultCustomerId.
 * @param items The list of OrderItemDto. Defaults to a new list of OrderItem
 * @param estimatedDeliveryTime The estimated delivery time for DELIVERY orders. Optional.
 * @param deliveryAddress The delivery address for DELIVERY orders. Optional.
 * @param bellName The bell name for DELIVERY orders. Optional.
 * @param customerPhone The customer phone number for DELIVERY orders. Optional.
 * @param pickupTime The pickup time for TAKEAWAY orders. Optional.
 * @param customerName The customer name for TAKEAWAY orders. Optional.
 * @param tableNumber The table number for DINE_IN orders. Optional.
 * @param numberOfGuests The number of guests for DINE_IN orders. Optional.
 * @return A PlaceOrderRequest with the given parameters.
 */
fun createTestPlaceOrderRequest(
  orderType: OrderType,
  restaurantId: String = defaultRestaurantId.value,
  customerId: String = defaultCustomerId.value,
  items: List<OrderItemDto> = createItemsDto(),
  estimatedDeliveryTime: Long? = null,
  deliveryAddress: String? = null,
  bellName: String? = null,
  customerPhone: String? = null,
  pickupTime: Long? = null,
  customerName: String? = null,
  tableNumber: Int? = null,
  numberOfGuests: Int? = null,
) = PlaceOrderRequest(
  orderType = orderType,
  restaurantId = restaurantId,
  customerId = customerId,
  items = items,
  estimatedDeliveryTime = estimatedDeliveryTime,
  deliveryAddress = deliveryAddress,
  bellName = bellName,
  customerPhone = customerPhone,
  pickupTime = pickupTime,
  customerName = customerName,
  tableNumber = tableNumber,
  numberOfGuests = numberOfGuests,
)

/** Creates a DiscardOrderRequest from an OrderId.
 * @param orderId The OrderId of the order to discard. Defaults to defaultOrderId.
 * @return A DiscardOrderRequest with the given OrderId and defaultCustomerId.
 */
fun createDiscardOrderRequest(
  orderId: OrderId = defaultOrderId,
  customerId: CustomerId = defaultCustomerId,
) = DiscardOrderRequest(
  orderId = orderId.value,
  customerId = customerId.value,
)

/** Creates an UpdateTakeawayOrderRequest from a TakeawayOrder.
 * @param order The TakeawayOrder to convert to an UpdateTakeawayOrderRequest. Defaults to a new TakeawayOrder.
 * @return An UpdateTakeawayOrderRequest with the same data as the TakeawayOrder.
 */
fun createUpdateTakeawayOrderRequest(order: TakeawayOrder = createTakeawayOrder()) =
  UpdateTakeawayOrderRequest(
    orderId = order.id.value,
    customerId = order.customerId.value,
    pickupTime = order.takeawayInfo.pickupTime,
    customerName = order.takeawayInfo.customerName,
  )

/** Creates an UpdateDeliveryOrderRequest from a DeliveryOrder.
 * @param order The DeliveryOrder to convert to an UpdateDeliveryOrderRequest. Defaults to a new DeliveryOrder.
 * @return An UpdateDeliveryOrderRequest with the same data as the DeliveryOrder.
 */
fun createUpdateDeliveryOrderRequest(order: DeliveryOrder = createDeliveryOrder()) =
  UpdateDeliveryOrderRequest(
    orderId = order.id.value,
    customerId = order.customerId.value,
    estimatedDeliveryTime = order.deliveryInfo.estimatedDeliveryTime,
    deliveryAddress = order.deliveryInfo.deliveryAddress,
    bellName = order.deliveryInfo.bellName,
    customerPhone = order.deliveryInfo.customerPhone,
  )

/**
 * Creates an UpdateOrderItemsRequest from a DeliveryOrder and a list of OrderItemDto.
 * @param order The DeliveryOrder to convert to an UpdateOrderItemsRequest. Defaults to a new DeliveryOrder.
 * @param items The list of OrderItemDto to include in the request. Defaults to a new list of OrderItemDto.
 * @return An UpdateOrderItemsRequest with the same data as the DeliveryOrder and the given items.
 */
fun createUpdateOrderItemsRequest(
  order: DeliveryOrder = createDeliveryOrder(),
  items: List<OrderItemDto> = createItemsDto(),
) = UpdateOrderItemsRequest(
  orderId = order.id.value,
  customerId = order.customerId.value,
  items = items,
)

/** Creates an AdvanceOrderStatusRequest from an OrderId.
 * @param orderId The OrderId of the order to advance. Defaults to defaultOrderId
 * @return An AdvanceOrderStatusRequest with the given OrderId.
 */
fun createAdvanceOrderStatusRequest(orderId: OrderId = defaultOrderId) = AdvanceOrderStatusRequest(
  orderId = orderId.value,
)
