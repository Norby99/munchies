package com.munchies.order.fixtures

import com.munchies.order.domain.model.MenuItemId
import com.munchies.order.domain.model.OrderItem
import com.munchies.order.domain.model.Price
import com.munchies.order.domain.model.RequestedItem
import com.munchies.order.infrastructure.adapter.dto.OrderItemDto

val orderItem1 = OrderItem(MenuItemId("item-1"), 2, Price(950))
val orderItem2 = OrderItem(MenuItemId("item-2"), 3, Price(400))

val invalidOrderItem1 = OrderItem(MenuItemId("item-1"), 0, Price(950))
val invalidOrderItem2 = OrderItem(MenuItemId("item-2"), -1, Price(400))

/** A valid, non-empty list of order items. */
fun createNewItems(): List<OrderItem> = listOf(orderItem1, orderItem2)

/** An empty item list — used to test the EmptyItems failure case. */
fun createEmptyItems(): List<OrderItem> = emptyList()

/** An item list containing one item with zero quantity. */
fun createInvalidItemsZeroCount(): List<OrderItem> = listOf(orderItem1, invalidOrderItem1)

/** An item list containing one item with negative quantity. */
fun createInvalidItemsNegativeCount(): List<OrderItem> = listOf(orderItem1, invalidOrderItem2)

/** Creates a list of OrderItemDto from a list of OrderItem. */
fun createItemsDto(items: List<OrderItem> = createNewItems()): List<OrderItemDto> = items.map {
  OrderItemDto(
    menuItemId = it.menuItemId.value,
    quantity = it.quantity,
  )
}

/**
 * The client-side view of [items]: what a command carries before order-service prices it. The
 * fixture prices above are the ones [menuPrices] returns, so pricing these yields [items] back.
 */
fun requested(items: List<OrderItem> = createNewItems()): List<RequestedItem> =
  items.map { RequestedItem(it.menuItemId, it.quantity) }

/** Menu prices matching the fixture items, as restaurant-service would return them. */
fun menuPrices(items: List<OrderItem> = createNewItems()): Map<MenuItemId, Price> =
  items.associate { it.menuItemId to it.unitPrice }
