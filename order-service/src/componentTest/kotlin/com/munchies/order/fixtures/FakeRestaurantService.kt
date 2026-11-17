package com.munchies.order.fixtures

import com.munchies.restaurant.infrastructure.adapter.dto.CategoryDto
import com.munchies.restaurant.infrastructure.adapter.dto.MenuDto
import com.munchies.restaurant.infrastructure.adapter.dto.MenuItemDto
import com.munchies.restaurant.infrastructure.adapter.dto.MenuSummaryDto
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.GetMenuResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.GetRestaurantMenusResponse
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress

/**
 * Stand-in for restaurant-service in component tests: a tiny HTTP server answering the menu
 * endpoints order-service reads, with the same JSON contracts (built from `restaurant-shared`).
 *
 * Component tests mock every other microservice and nothing else, so order-service's real
 * client, parsing and price mapping run against it. Every restaurant id serves one menu with the
 * fixture items (see [menuPrices]); [failing] makes it answer `500` to every request.
 */
class FakeRestaurantService {
  private val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)

  @Volatile var failing = false

  val url: String get() = "http://127.0.0.1:${server.address.port}"

  init {
    server.createContext("/") { exchange ->
      val path = exchange.requestURI.path
      val (status, body) = when {
        failing -> 500 to """{"result":"boom","code":500}"""
        MENU.matches(path) -> 200 to menu().toJson()
        MENUS.matches(path) -> 200 to GetRestaurantMenusResponse(
          arrayOf(MenuSummaryDto(MENU_ID, "Lunch")),
        ).toJson()
        else -> 404 to """{"result":"Not found","code":404}"""
      }
      val bytes = body.toByteArray()
      exchange.responseHeaders.add("Content-Type", "application/json")
      exchange.sendResponseHeaders(status, bytes.size.toLong())
      exchange.responseBody.use { it.write(bytes) }
    }
    server.start()
  }

  private fun menu() = GetMenuResponse(
    MenuDto(
      MENU_ID,
      "Lunch",
      arrayOf(
        CategoryDto(
          "cat-1",
          "Pasta",
          menuPrices(createNewItemsBigger()).map { (id, price) ->
            MenuItemDto(
              id.value,
              id.value,
              "",
              "%d.%02d".format(price.cents / 100, price.cents % 100),
            )
          }.toTypedArray(),
        ),
      ),
    ),
  )

  private companion object {
    const val MENU_ID = "menu-1"
    val MENUS = Regex("/restaurant/[^/]+/menus/?")
    val MENU = Regex("/restaurant/[^/]+/menus/$MENU_ID/?")
  }
}
