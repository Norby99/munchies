package com.munchies.e2e.steps

import com.munchies.e2e.fixtures.user.UserCredentials
import com.munchies.e2e.support.ServiceUrls
import com.munchies.e2e.support.WordResult
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.CreateMenuRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.CreateCategoryRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.category.createCategoryResponseFromJson
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.createMenuResponseFromJson
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.CreateMenuItemRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem.createMenuItemResponseFromJson
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.CreateRestaurantRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.createRestaurantResponseFromJson
import com.munchies.restaurant.infrastructure.adapter.inbound.web.config.MenuServiceConfig
import com.munchies.restaurant.infrastructure.adapter.inbound.web.config.RestaurantServiceConfig
import io.cucumber.java.en.Given
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.http.cookie.Cookie
import java.util.UUID

/**
 * Sets up, through the gateway, the restaurant data an order needs: order-service prices orders
 * from restaurant-service's menus, so a scenario that places an order must first have a real
 * restaurant with a dish on a menu. A dedicated manager owns it; their session is kept local, so
 * the scenario's own client (the customer in [WordResult.authCookie]) is left untouched.
 */
class RestaurantSteps(private val world: WordResult) {

  private val client = HttpClient.create(java.net.URI(ServiceUrls.gateway).toURL())

  @Given("a restaurant with a dish on its menu")
  fun aRestaurantWithADishOnItsMenu() {
    val manager = registerManager()
    val id = UUID.randomUUID().toString().take(8)

    val restaurantId = createRestaurantResponseFromJson(
      post(
        manager,
        RestaurantServiceConfig.SERVICE_PATH + RestaurantServiceConfig.CREATE_RESTAURANT_PATH,
        CreateRestaurantRequest(
          name = "E2E Trattoria $id",
          address = "Via Roma 1, Cesena",
          phone = "0547123456",
          email = "restaurant-$id@e2e-test.local",
        ).toJson(),
      ),
    ).result

    val menuPath = MenuServiceConfig.SERVICE_PATH + MenuServiceConfig.CREATE_MENU_PATH
      .replace("{restaurantId}", restaurantId)
    val menuId = createMenuResponseFromJson(
      post(manager, menuPath, CreateMenuRequest(name = "Lunch").toJson()),
    ).result.id

    val categoryId = createCategoryResponseFromJson(
      post(
        manager,
        "$menuPath/$menuId/categories",
        CreateCategoryRequest(name = "Burgers").toJson(),
      ),
    ).result.id

    world.menuItemId = createMenuItemResponseFromJson(
      post(
        manager,
        "$menuPath/$menuId/categories/$categoryId/items",
        CreateMenuItemRequest(
          name = "Super double",
          description = "Two patties",
          price = "9.50",
        ).toJson(),
      ),
    ).result
    world.restaurantId = restaurantId
  }

  private fun registerManager(): Cookie {
    val response = client.toBlocking().exchange(
      HttpRequest.POST(
        "/users/register",
        UserCredentials.newManager(),
      ).contentType("application/json"),
      String::class.java,
    )
    return response.cookies["authToken"] ?: error("Manager registered but no auth cookie was given")
  }

  private fun post(manager: Cookie, path: String, body: String): String =
    client.toBlocking().retrieve(
      HttpRequest.POST(path, body).cookie(manager).contentType("application/json"),
      String::class.java,
    )
}
