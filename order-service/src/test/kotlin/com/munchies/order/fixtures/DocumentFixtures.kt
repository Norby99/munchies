package com.munchies.order.fixtures

import com.munchies.order.domain.model.OrderStatus
import com.munchies.order.infrastructure.adapter.dto.OrderType
import com.munchies.order.infrastructure.adapter.outbound.mongo.document.DeliveryInfoDocument
import com.munchies.order.infrastructure.adapter.outbound.mongo.document.OrderDocument
import com.munchies.order.infrastructure.adapter.outbound.mongo.document.OrderItemDocument
import com.munchies.order.infrastructure.adapter.outbound.mongo.document.TableInfoDocument
import com.munchies.order.infrastructure.adapter.outbound.mongo.document.TakeawayInfoDocument

/**
 * Creates a sample OrderDocument for testing purposes.
 *
 * @param orderType The type of the order (e.g., DELIVERY, DINE_IN, TAKEAWAY).
 * @param status The status of the order (e.g., PENDING, COMPLETED).
 * @param deliveryInfo Optional delivery information for delivery orders.
 * @param tableInfo Optional table information for dine-in orders.
 * @param takeawayInfo Optional takeaway information for takeaway orders.
 * @return An instance of OrderDocument with the specified parameters.
 */
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

/**
 * Creates a sample DineInOrderDocument for testing purposes.
 *
 * @param orderType The type of the order (default is DINE_IN).
 * @param status The status of the order (default is PENDING).
 * @param tableInfo Optional table information for dine-in orders.
 * @return An instance of OrderDocument representing a dine-in order.
 */
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

/**
 * Creates a sample TakeawayOrderDocument for testing purposes.
 *
 * @param orderType The type of the order (default is TAKEAWAY).
 * @param status The status of the order (default is PENDING).
 * @param takeawayInfo Optional takeaway information for takeaway orders.
 * @return An instance of OrderDocument representing a takeaway order.
 */
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

/**
 * Creates a sample OrderItemDocument for testing purposes.
 *
 * @param menuItemId The ID of the menu item (default is the default order ID).
 * @param quantity The quantity of the menu item (default is 1).
 * @return An instance of OrderItemDocument with the specified parameters.
 */
fun createOrderItemDocument(menuItemId: String = defaultOrderId.value, quantity: Int = 1) =
  OrderItemDocument(
    menuItemId = menuItemId,
    quantity = quantity,
  )
