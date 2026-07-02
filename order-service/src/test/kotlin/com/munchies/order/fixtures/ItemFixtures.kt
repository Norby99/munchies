package com.munchies.order.fixtures

import com.munchies.order.domain.model.MenuItemId
import com.munchies.order.domain.model.OrderItem

val orderItem1 = OrderItem(MenuItemId("item-1"), 2)
val orderItem2 = OrderItem(MenuItemId("item-2"), 3)

val invalidOrderItem1 = OrderItem(MenuItemId("item-1"), 0)
val invalidOrderItem2 = OrderItem(MenuItemId("item-2"), -1)

/** A valid, non-empty list of order items. */
fun createNewItems(): List<OrderItem> = listOf(orderItem1, orderItem2)

/** An empty item list — used to test the EmptyItems failure case. */
fun createEmptyItems(): List<OrderItem> = emptyList()

/** An item list containing one item with zero quantity. */
fun createInvalidItemsZeroCount(): List<OrderItem> = listOf(orderItem1, invalidOrderItem1)

/** An item list containing one item with negative quantity. */
fun createInvalidItemsNegativeCount(): List<OrderItem> = listOf(orderItem1, invalidOrderItem2)
