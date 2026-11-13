package com.munchies.restaurant.infrastructure.adapter.inbound.web.config

import kotlin.js.JsExport

@JsExport
object RestaurantServiceConfig {
  const val SERVICE_NAME = "restaurant-service"
  const val SERVICE_PATH = "/restaurants/"
  const val CREATE_RESTAURANT_PATH = ""

  /** Customer search: `GET /restaurants/?q=<text>` (same path as creation, different method). */
  const val SEARCH_RESTAURANTS_PATH = ""
  const val SEARCH_QUERY_PARAM = "q"
  const val GET_RESTAURANT_PATH = "{restaurantId}/"
  const val MANAGER_RESTAURANTS_PREFIX = "manager/"
  const val GET_MANAGER_RESTAURANTS_PATH = MANAGER_RESTAURANTS_PREFIX + "{managerId}/"
  const val UPDATE_RESTAURANT_PATH = "{restaurantId}/"
  const val DELETE_RESTAURANT_PATH = "{restaurantId}/"
  const val CAPACITY_POLICY_PATH = "{restaurantId}/capacity-policy/"
  const val RESTAURANT_LOCATION_PATH = "{restaurantId}/location/"
  const val TABLES_PATH = "{restaurantId}/tables/"

  const val SERVICE_PORT = 8080
}
