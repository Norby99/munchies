package com.munchies.restaurant.infrastructure.adapter.inbound.web.config

import kotlin.js.JsExport

@JsExport
object MenuServiceConfig {
  const val SERVICE_NAME = "restaurant-service"
  const val SERVICE_PORT = 8080
  const val SERVICE_PATH = "/restaurant/"

  const val CREATE_MENU_PATH = "{restaurantId}/menus"
  const val GET_MENU_PATH = "{restaurantId}/menus/{menuId}"
  const val GET_RESTAURANT_MENUS_PATH = "{restaurantId}/menus"
  const val UPDATE_MENU_PATH = "{restaurantId}/menus/{menuId}"
  const val DELETE_MENU_PATH = "{restaurantId}/menus/{menuId}"

  const val CREATE_CATEGORY_PATH = "{restaurantId}/menus/{menuId}/categories"
  const val UPDATE_CATEGORY_PATH = "{restaurantId}/menus/{menuId}/categories/{categoryId}"
  const val DELETE_CATEGORY_PATH = "{restaurantId}/menus/{menuId}/categories/{categoryId}"

  const val CREATE_MENU_ITEM_PATH = "{restaurantId}/menus/{menuId}/categories/{categoryId}/items"
  const val UPDATE_MENU_ITEM_PATH =
    "{restaurantId}/menus/{menuId}/categories/{categoryId}/items/{itemId}"
  const val REMOVE_MENU_ITEM_PATH =
    "{restaurantId}/menus/{menuId}/categories/{categoryId}/items/{itemId}"
}
