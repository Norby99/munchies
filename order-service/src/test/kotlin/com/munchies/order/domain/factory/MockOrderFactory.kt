package com.munchies.order.domain.factory

import com.munchies.order.domain.model.CustomerId
import com.munchies.order.domain.model.DeliveryInfo
import com.munchies.order.domain.model.MenuItemId
import com.munchies.order.domain.model.OrderId
import com.munchies.order.domain.model.OrderItem
import com.munchies.order.domain.model.RestaurantId
import com.munchies.order.domain.model.TableInfo
import com.munchies.order.domain.model.TakeawayInfo

object MockOrderFactory {

  val defaultOrderId = OrderId("order-123")
  val defaultRestaurantId = RestaurantId("rest-456")
  val defaultCustomerId = CustomerId("cust-789")

  val futureTime get() = System.currentTimeMillis() + 3600000
  val pastTime get() = System.currentTimeMillis() - 3600000

  val validItems = listOf(
    OrderItem(MenuItemId("item-1"), 2),
    OrderItem(MenuItemId("item-2"), 1),
  )
  val emptyItems = emptyList<OrderItem>()
  val invalidQuantityItems = listOf(
    OrderItem(MenuItemId("item-1"), 2),
    OrderItem(MenuItemId("item-1"), -1),
  )

  /**
   * Creates a mock DeliveryInfo object with the specified estimated delivery time.
   *
   * @param estimatedDeliveryTime The estimated delivery time in milliseconds since epoch.
   * @return A DeliveryInfo object with the specified estimated delivery time.
   */
  fun createDeliveryInfo(
    estimatedDeliveryTime: Long = futureTime,
    deliveryAddress: String = "Via Roma 1",
    bellName: String = "Rossi",
    customerPhone: String = "123",
  ) = DeliveryInfo(estimatedDeliveryTime, deliveryAddress, bellName, customerPhone)

  /**
   * Creates a mock TakeawayInfo object with the specified estimated pickup time.
   *
   * @param estimatedPickupTime The estimated pickup time in milliseconds since epoch.
   * @return A TakeawayInfo object with the specified estimated pickup time.
   */
  fun createTakeawayInfo(pickupTime: Long = futureTime, customerName: String = "Luigi") =
    TakeawayInfo(pickupTime, customerName)

  /**
   * Creates a mock TableInfo object with the specified table number and number of guests.
   *
   * @param tableNumber The table number.
   * @param numberOfGuests The number of guests at the table.
   * @return A TableInfo object with the specified table number and number of guests.
   */
  fun createTableInfo(tableNumber: Int = 5, numberOfGuests: Int = 4) =
    TableInfo(tableNumber, numberOfGuests)
}
