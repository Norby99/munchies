package com.munchies.order.domain.port

import com.munchies.order.domain.model.MenuItemId
import com.munchies.order.domain.model.Price
import com.munchies.order.domain.model.RestaurantId

/**
 * Outbound port to the restaurant's menus, owned by restaurant-service.
 *
 * order-service uses it to price the items of an order when they are placed or changed, so the
 * price stored in the order (and later charged by payment-service) always comes from the menu,
 * never from the client.
 */
interface MenuCatalog {

  /**
   * Looks up the current price of [menuItemIds] in the menus of [restaurantId].
   *
   * Items that are not on any menu of that restaurant are simply missing from the returned map:
   * deciding whether that is an error is up to the caller.
   */
  fun findPrices(restaurantId: RestaurantId, menuItemIds: Set<MenuItemId>): PriceLookup

  /** Outcome of [findPrices]. */
  sealed interface PriceLookup {
    /** The restaurant exists; [prices] holds every requested item found on its menus. */
    data class Found(val prices: Map<MenuItemId, Price>) : PriceLookup

    /** No restaurant with that id. */
    data object RestaurantNotFound : PriceLookup

    /** restaurant-service could not be reached or answered with an unexpected error. */
    data object Unavailable : PriceLookup
  }
}
