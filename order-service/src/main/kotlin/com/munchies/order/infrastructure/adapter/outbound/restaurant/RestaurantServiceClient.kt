package com.munchies.order.infrastructure.adapter.outbound.restaurant

import com.munchies.restaurant.infrastructure.adapter.inbound.web.config.MenuServiceConfig
import com.munchies.restaurant.infrastructure.adapter.inbound.web.config.RestaurantServiceConfig
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.client.annotation.Client

/**
 * Declarative Micronaut HTTP client for the restaurant-service endpoints order-service reads.
 *
 * The base URL and timeouts come from `micronaut.http.services.restaurant-service` in
 * `application.yml`. Paths are reused from `restaurant-shared`, so they cannot drift from the
 * real controller. Bodies are returned raw and decoded with the shared kotlinx parsers (the same
 * contract classes restaurant-service serializes), instead of registering them with Micronaut
 * serde. A `404` yields `null`.
 */
@Client(id = RestaurantServiceConfig.SERVICE_NAME)
interface RestaurantServiceClient {

  @Get(RestaurantServiceConfig.SERVICE_PATH + RestaurantServiceConfig.GET_RESTAURANT_PATH)
  fun getRestaurant(@PathVariable restaurantId: String): String?

  @Get(MenuServiceConfig.SERVICE_PATH + MenuServiceConfig.GET_RESTAURANT_MENUS_PATH)
  fun getRestaurantMenus(@PathVariable restaurantId: String): String?

  @Get(MenuServiceConfig.SERVICE_PATH + MenuServiceConfig.GET_MENU_PATH)
  fun getMenu(@PathVariable restaurantId: String, @PathVariable menuId: String): String?
}
