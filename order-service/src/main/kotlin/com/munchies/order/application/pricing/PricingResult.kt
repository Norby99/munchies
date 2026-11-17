package com.munchies.order.application.pricing

import com.munchies.order.domain.model.MenuItemId
import com.munchies.order.domain.model.OrderItem
import com.munchies.order.domain.model.RequestedItem
import com.munchies.order.domain.model.RestaurantId
import com.munchies.order.domain.port.MenuCatalog
import com.munchies.order.domain.port.MenuCatalog.PriceLookup

/**
 * Pricing step shared by the use cases that set the items of an order (place and update):
 * every requested item must be on the restaurant's menus, and takes its price from there.
 */
internal sealed interface PricingResult {
  data class Priced(val items: List<OrderItem>) : PricingResult

  /** Why the requested items cannot become order items. */
  sealed interface Rejected : PricingResult
  data object EmptyItems : Rejected
  data object InvalidItemQuantity : Rejected
  data class UnknownItems(val menuItemIds: Set<MenuItemId>) : Rejected
  data object RestaurantNotFound : Rejected
  data object CatalogUnavailable : Rejected
}

/**
 * Checks [requested] and prices it against the menus of [restaurantId].
 *
 * The quantity checks need no remote call, so they run first: an obviously invalid request never
 * reaches restaurant-service. They mirror [com.munchies.order.domain.model.Order.validateItems],
 * which still validates the priced items when the order is created or changed.
 */
internal fun MenuCatalog.checkAndPrice(
  restaurantId: RestaurantId,
  requested: List<RequestedItem>,
): PricingResult = when {
  requested.isEmpty() -> PricingResult.EmptyItems
  requested.any { !it.isValid() } -> PricingResult.InvalidItemQuantity
  else -> price(restaurantId, requested)
}

private fun MenuCatalog.price(
  restaurantId: RestaurantId,
  requested: List<RequestedItem>,
): PricingResult {
  val ids = requested.map { it.menuItemId }.toSet()
  return when (val lookup = findPrices(restaurantId, ids)) {
    is PriceLookup.RestaurantNotFound -> PricingResult.RestaurantNotFound
    is PriceLookup.Unavailable -> PricingResult.CatalogUnavailable
    is PriceLookup.Found -> {
      val unknown = ids - lookup.prices.keys
      if (unknown.isNotEmpty()) {
        PricingResult.UnknownItems(unknown)
      } else {
        PricingResult.Priced(requested.map { it.pricedAt(lookup.prices.getValue(it.menuItemId)) })
      }
    }
  }
}
