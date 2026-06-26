package com.munchies.order.fixtures

import com.munchies.order.domain.model.*

// ---------- Time helpers ----------

private val fixedNow: Long = System.currentTimeMillis()
val pastTime: Long get() = fixedNow - 60_000
val futureTime: Long get() = fixedNow + 60_000

// ---------- IDs ----------

val defaultOrderId: OrderId get() = OrderId("o1")
val defaultRestaurantId: RestaurantId get() = RestaurantId("r-1")
val defaultCustomerId: CustomerId get() = CustomerId("c-1")

// ---------- Reusable contact presets ----------

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

// ---------- Info builders ----------

fun createDeliveryInfo(
  estimatedDeliveryTime: Long = futureTime,
  address: Address = Address1,
): DeliveryInfo = DeliveryInfo(
  estimatedDeliveryTime = estimatedDeliveryTime,
  deliveryAddress = address.deliveryAddress,
  bellName = address.bellName,
  customerPhone = address.customerPhone,
)

fun createTakeawayInfo(
  pickupTime: Long = futureTime,
  customerName: String = "Bianchi",
): TakeawayInfo = TakeawayInfo(
  pickupTime = pickupTime,
  customerName = customerName,
)

fun defaultTableInfo(tableNumber: Int = 1, numberOfGuests: Int = 2): TableInfo = TableInfo(
  tableNumber = tableNumber,
  numberOfGuests = numberOfGuests,
)

val tableInfo1: TableInfo get() = defaultTableInfo(tableNumber = 1, numberOfGuests = 2)
val tableInfo2: TableInfo get() = defaultTableInfo(tableNumber = 2, numberOfGuests = 4)

// ---------- Order builders ----------

/**
 * Creates a default DeliveryOrder with valid items, using [Address1] by default.
 * @param status The status of the order. Defaults to PENDING.
 * @param items The items of the order. Defaults to a valid non-empty list.
 */
fun createDeliveryOrder(
  status: OrderStatus = OrderStatus.PENDING,
  items: List<OrderItem> = createNewItems(),
  deliveryInfo: DeliveryInfo = createDeliveryInfo(),
): DeliveryOrder = DeliveryOrder(
  id = defaultOrderId,
  restaurantId = defaultRestaurantId,
  customerId = defaultCustomerId,
  status = status,
  items = items,
  deliveryInfo = deliveryInfo,
)

/**
 * Creates a default TakeawayOrder with valid items.
 * @param status The status of the order. Defaults to PENDING.
 * @param items The items of the order. Defaults to a valid non-empty list.
 */
fun createTakeawayOrder(
  status: OrderStatus = OrderStatus.PENDING,
  items: List<OrderItem> = createNewItems(),
  takeawayInfo: TakeawayInfo = createTakeawayInfo(),
): TakeawayOrder = TakeawayOrder(
  id = defaultOrderId,
  restaurantId = defaultRestaurantId,
  customerId = defaultCustomerId,
  status = status,
  items = items,
  takeawayInfo = takeawayInfo,
)

/**
 * Creates a default DineInOrder with valid items.
 * @param status The status of the order. Defaults to PENDING.
 * @param items The items of the order. Defaults to a valid non-empty list.
 */
fun createDineInOrder(
  status: OrderStatus = OrderStatus.PENDING,
  items: List<OrderItem> = createNewItems(),
  tableInfo: TableInfo = tableInfo1,
): DineInOrder = DineInOrder(
  id = defaultOrderId,
  restaurantId = defaultRestaurantId,
  customerId = defaultCustomerId,
  status = status,
  items = items,
  tableInfo = tableInfo,
)

/**
 * Creates a default Order with valid items, using [Address1] by default.
 * @param status The status of the order. Defaults to PENDING.
 * @param items The items of the order. Defaults to a valid non-empty list.
 */
fun createSampleOrder(status: OrderStatus = OrderStatus.PENDING) = TakeawayOrder(
  id = defaultOrderId,
  restaurantId = defaultRestaurantId,
  customerId = defaultCustomerId,
  status = status,
  items = createNewItems(),
  takeawayInfo = createTakeawayInfo(),
)
