package com.munchies.order.fixtures

import com.munchies.order.domain.model.MenuItemId
import com.munchies.order.domain.model.OrderItem

/** A valid, non-empty list of order items. */
fun createNewItems(): List<OrderItem> = listOf(
  OrderItem(MenuItemId("item-1"), 2),
  OrderItem(MenuItemId("item-2"), 3),
)

/** An empty item list — used to test the EmptyItems failure case. */
fun createEmptyItems(): List<OrderItem> = emptyList()

/** An item list containing one item with zero quantity. */
fun createInvalidItemsZeroCount(): List<OrderItem> = listOf(
  OrderItem(MenuItemId("item-1"), 2),
  OrderItem(MenuItemId("item-2"), 0),
)

/** An item list containing one item with negative quantity. */
fun createInvalidItemsNegativeCount(): List<OrderItem> = listOf(
  OrderItem(MenuItemId("item-1"), 2),
  OrderItem(MenuItemId("item-1"), -1),
)
