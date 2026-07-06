package com.munchies.order.fixtures

import com.munchies.order.domain.model.OrderStatus
import com.munchies.order.infrastructure.adapter.dto.OrderType
import com.munchies.order.infrastructure.adapter.outbound.mongo.document.DeliveryInfoDocument
import com.munchies.order.infrastructure.adapter.outbound.mongo.document.OrderDocument
import com.munchies.order.infrastructure.adapter.outbound.mongo.document.OrderItemDocument
import com.munchies.order.infrastructure.adapter.outbound.mongo.document.TableInfoDocument
import com.munchies.order.infrastructure.adapter.outbound.mongo.document.TakeawayInfoDocument

fun createDeliveryOrderDocument(
  orderType: String = OrderType.DELIVERY.name,
  status: String = OrderStatus.PENDING.name,
  deliveryInfo: DeliveryInfoDocument? = DeliveryInfoDocument(
    estimatedDeliveryTime = futureTime,
    deliveryAddress = Address1.deliveryAddress,
    bellName = Address1.bellName,
    customerPhone = Address1.customerPhone,
  ),
) = OrderDocument(
  id = defaultOrderId.value,
  orderType = orderType,
  restaurantId = defaultRestaurantId.value,
  customerId = defaultCustomerId.value,
  status = status,
  items = listOf(createOrderItemDocument()),
  deliveryInfo = deliveryInfo,
  tableInfo = null,
  takeawayInfo = null,
)

fun createDineInOrderDocument(
  orderType: String = OrderType.DINE_IN.name,
  status: String = OrderStatus.PENDING.name,
  tableInfo: TableInfoDocument? = TableInfoDocument(
    tableNumber = 5,
    numberOfGuests = 2,
  ),
) = OrderDocument(
  id = defaultOrderId.value,
  orderType = orderType,
  restaurantId = defaultRestaurantId.value,
  customerId = defaultCustomerId.value,
  status = status,
  items = listOf(createOrderItemDocument()),
  deliveryInfo = null,
  tableInfo = tableInfo,
  takeawayInfo = null,
)

fun createTakeawayOrderDocument(
  orderType: String = OrderType.TAKEAWAY.name,
  status: String = OrderStatus.PENDING.name,
  takeawayInfo: TakeawayInfoDocument? = TakeawayInfoDocument(
    pickupTime = futureTime,
    customerName = Address1.customerPhone,
  ),
) = OrderDocument(
  id = defaultOrderId.value,
  orderType = orderType,
  restaurantId = defaultRestaurantId.value,
  customerId = defaultCustomerId.value,
  status = status,
  items = listOf(createOrderItemDocument()),
  deliveryInfo = null,
  tableInfo = null,
  takeawayInfo = takeawayInfo,
)

fun createOrderItemDocument(menuItemId: String = defaultOrderId.value, quantity: Int = 1) =
  OrderItemDocument(
    menuItemId = menuItemId,
    quantity = quantity,
  )
