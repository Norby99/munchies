package com.munchies.order.fixtures

import com.munchies.order.application.port.inbound.command.PlaceOrderCommand
import com.munchies.order.application.port.inbound.command.UpdateDeliveryOrderCommand
import com.munchies.order.application.port.inbound.command.UpdateOrderItemsCommand
import com.munchies.order.application.port.inbound.command.UpdateTakeawayOrderCommand
import com.munchies.order.domain.model.*

fun deliveryCommand(
  restaurantId: RestaurantId = RestaurantId("rest-1"),
  customerId: CustomerId = CustomerId("cust-1"),
  items: List<OrderItem> = listOf(OrderItem(MenuItemId("item-1"), 2)),
  estimatedDeliveryTime: Long = 1_000L,
  deliveryAddress: String = "Via Roma 1",
  bellName: String = "Rossi",
  customerPhone: String = "+393331234567",
): PlaceOrderCommand.Delivery = PlaceOrderCommand.Delivery(
  restaurantId = restaurantId,
  customerId = customerId,
  items = items,
  estimatedDeliveryTime = estimatedDeliveryTime,
  deliveryAddress = deliveryAddress,
  bellName = bellName,
  customerPhone = customerPhone,
)

fun takeawayCommand(
  restaurantId: RestaurantId = RestaurantId("rest-1"),
  customerId: CustomerId = CustomerId("cust-1"),
  items: List<OrderItem> = listOf(OrderItem(MenuItemId("item-1"), 2)),
  pickupTime: Long = 1_000L,
  customerName: String = "Bianchi",
): PlaceOrderCommand.Takeaway = PlaceOrderCommand.Takeaway(
  restaurantId = restaurantId,
  customerId = customerId,
  items = items,
  pickupTime = pickupTime,
  customerName = customerName,
)

fun dineInCommand(
  restaurantId: RestaurantId = RestaurantId("rest-1"),
  customerId: CustomerId = CustomerId("cust-1"),
  items: List<OrderItem> = listOf(OrderItem(MenuItemId("item-1"), 2)),
  tableNumber: Int = 5,
  numberOfGuests: Int = 2,
): PlaceOrderCommand.DineIn = PlaceOrderCommand.DineIn(
  restaurantId = restaurantId,
  customerId = customerId,
  items = items,
  tableNumber = tableNumber,
  numberOfGuests = numberOfGuests,
)

fun createUpdateDeliveryOrderInfoCommand(estimatedDeliveryTime: Long = futureTime) =
  UpdateDeliveryOrderCommand(
    orderId = defaultOrderId,
    customerId = defaultCustomerId,
    estimatedDeliveryTime = estimatedDeliveryTime,
    deliveryAddress = "Nuova Via 10",
    bellName = "Verdi",
    customerPhone = "987654321",
  )

fun createUpdateOrderItemsCommand(items: List<OrderItem> = createNewItems()) =
  UpdateOrderItemsCommand(
    orderId = defaultOrderId,
    customerId = defaultCustomerId,
    items = items,
  )

fun createUpdateTakeawayOrderInfoUseCommand(pickupTime: Long = futureTime) =
  UpdateTakeawayOrderCommand(
    orderId = defaultOrderId,
    customerId = defaultCustomerId,
    pickupTime = pickupTime,
    customerName = "Giovanni Gialli",
  )
