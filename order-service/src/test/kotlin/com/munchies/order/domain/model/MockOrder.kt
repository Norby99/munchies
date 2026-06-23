package com.munchies.order.domain.model

import kotlin.String

val PAST_TIME = System.currentTimeMillis() - 60000
val FUTURE_TIME = System.currentTimeMillis() + 60000

val tableInfo1 = TableInfo(
  tableNumber = 1,
  numberOfGuests = 2,
)

val tableInfo2 = TableInfo(
  tableNumber = 2,
  numberOfGuests = 4,
)

sealed interface Address {
  val deliveryAddress: String
  val bellName: String
  val customerPhone: String
}

object Address1 : Address {
  override val deliveryAddress = "Via Roma 1"
  override val bellName = "Rossi"
  override val customerPhone = "123456"
}

object Address2 : Address {
  override val deliveryAddress = "Via Torino 2"
  override val bellName = "Gallo"
  override val customerPhone = "999"
}

/**
 * Creates a default DeliveryOrder from Address1 with the specified status.
 * @param status The status of the order. Defaults to PENDING.
 * @return A DeliveryOrder instance with the specified status and default values.
 */
fun defaultDeliveryOrder(status: OrderStatus = OrderStatus.PENDING): DeliveryOrder = DeliveryOrder(
  id = OrderId("o-1"),
  restaurantId = RestaurantId("r-1"),
  customerId = CustomerId("c-1"),
  status = status,
  items = listOf(),
  deliveryInfo = DeliveryInfo(
    estimatedDeliveryTime = FUTURE_TIME,
    deliveryAddress = Address1.deliveryAddress,
    bellName = Address1.bellName,
    customerPhone = Address1.customerPhone,
  ),
)

/**
 * Creates a default TakeawayOrder with the specified status.
 * @param status The status of the order. Defaults to PENDING.
 * @return A TakeawayOrder instance with the specified status and default values.
 */
fun defaultTakeawayOrder(status: OrderStatus = OrderStatus.PENDING): TakeawayOrder = TakeawayOrder(
  id = OrderId("o-1"),
  restaurantId = RestaurantId("r-1"),
  customerId = CustomerId("c-1"),
  status = status,
  items = listOf(),
  takeawayInfo = TakeawayInfo(
    pickupTime = FUTURE_TIME,
    customerName = Address1.bellName,
  ),
)

/**
 * Creates a default DeliveryOrder with the specified status and items.
 * @param status The status of the order. Defaults to PENDING.
 * @return A DeliveryOrder instance with the specified status, items, and default values.
 */
fun defaultDineInOrder(status: OrderStatus = OrderStatus.PENDING): DineInOrder = DineInOrder(
  id = OrderId("o-1"),
  restaurantId = RestaurantId("r-1"),
  customerId = CustomerId("c-1"),
  status = status,
  items = listOf(),
  tableInfo = tableInfo1,
)

/**
 * Creates a valid item list.
 * @return A list of OrderItem instances with valid quantities.
 */
fun defaultNewItems() = listOf(
  OrderItem(
    MenuItemId("item-1"),
    2,
  ),
  OrderItem(MenuItemId("item-2"), 3),
)

/**
 * Creates an empty item list.
 * @return An empty list of OrderItem instances.
 */
fun defaultEmptyItems() = listOf<OrderItem>()

/**
 * Creates an item list with an item having zero quantity.
 * @return A list of OrderItem instances with one item having zero quantity.
 */
fun defaultInvalidItemsZeroCount() = listOf(
  OrderItem(MenuItemId("item-1"), 2),
  OrderItem(MenuItemId("item-2"), 0),
)

/**
 * Creates an item list with an item having negative quantity.
 * @return A list of OrderItem instances with one item having negative quantity.
 */
fun defaultInvalidItemsNegativeCount() = listOf(
  OrderItem(MenuItemId("item-1"), 2),
  OrderItem(MenuItemId("item-1"), -1),
)
