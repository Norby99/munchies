package com.munchies.order.fixtures

import com.munchies.order.domain.model.*

fun defaultDeliveryOrder(
  id: OrderId = OrderId("order-1"),
  restaurantId: RestaurantId = RestaurantId("rest-1"),
  customerId: CustomerId = CustomerId("cust-1"),
  status: OrderStatus = OrderStatus.PENDING,
  items: List<OrderItem> = listOf(OrderItem(MenuItemId("item-1"), 2)),
  deliveryInfo: DeliveryInfo = DeliveryInfo(
    estimatedDeliveryTime = 1_000L,
    deliveryAddress = "Via Roma 1",
    bellName = "Rossi",
    customerPhone = "+393331234567",
  ),
): DeliveryOrder = DeliveryOrder(id, restaurantId, customerId, status, items, deliveryInfo)

fun defaultTakeawayOrder(
  id: OrderId = OrderId("order-1"),
  restaurantId: RestaurantId = RestaurantId("rest-1"),
  customerId: CustomerId = CustomerId("cust-1"),
  status: OrderStatus = OrderStatus.PENDING,
  items: List<OrderItem> = listOf(OrderItem(MenuItemId("item-1"), 2)),
  takeawayInfo: TakeawayInfo = TakeawayInfo(pickupTime = 1_000L, customerName = "Bianchi"),
): TakeawayOrder = TakeawayOrder(id, restaurantId, customerId, status, items, takeawayInfo)

fun defaultDineInOrder(
  id: OrderId = OrderId("order-1"),
  restaurantId: RestaurantId = RestaurantId("rest-1"),
  customerId: CustomerId = CustomerId("cust-1"),
  status: OrderStatus = OrderStatus.PENDING,
  items: List<OrderItem> = listOf(OrderItem(MenuItemId("item-1"), 2)),
  tableInfo: TableInfo = TableInfo(tableNumber = 5, numberOfGuests = 2),
): DineInOrder = DineInOrder(id, restaurantId, customerId, status, items, tableInfo)
