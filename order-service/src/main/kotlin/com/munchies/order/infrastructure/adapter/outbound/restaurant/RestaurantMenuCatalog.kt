package com.munchies.order.infrastructure.adapter.outbound.restaurant

import com.munchies.order.domain.model.MenuItemId
import com.munchies.order.domain.model.Price
import com.munchies.order.domain.model.RestaurantId
import com.munchies.order.domain.port.MenuCatalog
import com.munchies.order.domain.port.MenuCatalog.PriceLookup
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.getMenuResponseFromJson
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.getRestaurantMenusResponseFromJson
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.http.client.exceptions.HttpClientResponseException
import java.math.BigDecimal
import org.slf4j.LoggerFactory

/**
 * [MenuCatalog] backed by restaurant-service's REST API.
 *
 * restaurant-service has no "price these items" endpoint, so the adapter reads the restaurant's
 * menu list and then each menu, collecting the requested items. Restaurants have few menus, so
 * this stays a handful of calls per order. Notes on the mapping:
 * - an item appears on every menu that lists it, always with the same price (it is the same
 *   menu item), so the first match wins;
 * - variations' additional prices are ignored: orders do not carry variations yet;
 * - menu validity windows (e.g. a lunch-only menu) are not enforced yet: an item is orderable if
 *   it is on any menu of the restaurant;
 * - a restaurant with no menus may not exist at all (restaurant-service answers an empty list
 *   either way), so only then the restaurant itself is looked up, to tell the two cases apart.
 *
 * Every transport, status or decoding failure becomes [PriceLookup.Unavailable]: the caller
 * cannot price the order and must not place it with made-up prices.
 */
class RestaurantMenuCatalog(private val client: RestaurantServiceClient) : MenuCatalog {

  override fun findPrices(restaurantId: RestaurantId, menuItemIds: Set<MenuItemId>): PriceLookup =
    try {
      lookup(restaurantId.value, menuItemIds)
    } catch (e: HttpClientResponseException) {
      if (e.status == HttpStatus.NOT_FOUND) {
        PriceLookup.RestaurantNotFound
      } else {
        unavailable(restaurantId, e)
      }
    } catch (e: HttpClientException) {
      unavailable(restaurantId, e)
    } catch (e: IllegalArgumentException) {
      // An undecodable body (kotlinx SerializationException is an IllegalArgumentException) or a
      // menu price that is not a valid non-negative amount.
      unavailable(restaurantId, e)
    }

  private fun lookup(restaurantId: String, wanted: Set<MenuItemId>): PriceLookup {
    val menus = client.getRestaurantMenus(restaurantId)
      ?.let { getRestaurantMenusResponseFromJson(it).result }
      ?: return PriceLookup.RestaurantNotFound
    if (menus.isEmpty()) {
      return if (client.getRestaurant(restaurantId) == null) {
        PriceLookup.RestaurantNotFound
      } else {
        PriceLookup.Found(emptyMap())
      }
    }

    val prices = mutableMapOf<MenuItemId, Price>()
    // Lazy: menus are fetched one at a time, and no more once every wanted item is priced.
    menus.asSequence()
      .takeWhile { !prices.keys.containsAll(wanted) }
      // null: the menu was deleted between the two calls.
      .mapNotNull { summary -> client.getMenu(restaurantId, summary.id) }
      .flatMap { body -> getMenuResponseFromJson(body).result.categories.asSequence() }
      .flatMap { category -> category.items.asSequence() }
      .filter { MenuItemId(it.id) in wanted }
      .forEach { prices.putIfAbsent(MenuItemId(it.id), toPrice(it.price)) }
    return PriceLookup.Found(prices)
  }

  private fun unavailable(restaurantId: RestaurantId, cause: Exception): PriceLookup {
    logger.error("Cannot read the menus of restaurant {}: {}", restaurantId.value, cause.message)
    return PriceLookup.Unavailable
  }

  private companion object {
    val logger = LoggerFactory.getLogger(RestaurantMenuCatalog::class.java)

    /** Menu prices are decimal strings in euro (e.g. "9.50"); converts them to exact cents. */
    fun toPrice(decimal: String): Price {
      val cents = try {
        BigDecimal(decimal).movePointRight(2).longValueExact()
      } catch (e: ArithmeticException) {
        throw IllegalArgumentException("Price with more than two decimals: $decimal", e)
      } catch (e: NumberFormatException) {
        throw IllegalArgumentException("Not a price: $decimal", e)
      }
      return Price(cents)
    }
  }
}
