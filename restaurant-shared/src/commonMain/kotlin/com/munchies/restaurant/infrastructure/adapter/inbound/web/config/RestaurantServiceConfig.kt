package com.munchies.restaurant.infrastructure.adapter.inbound.web.config

import kotlin.js.JsExport

@JsExport
object RestaurantServiceConfig {
  const val SERVICE_NAME = "restaurant-service"
  const val SERVICE_PATH = "/restaurants/"
  const val CREATE_RESTAURANT_PATH = ""
  const val GET_RESTAURANT_PATH = "{restaurantId}/"
  const val GET_MANAGER_RESTAURANTS_PATH = "manager/{managerId}/"
  const val UPDATE_RESTAURANT_PATH = "{restaurantId}/"
  const val DELETE_RESTAURANT_PATH = "{restaurantId}/"

  const val SERVICE_PORT = 8080
}
