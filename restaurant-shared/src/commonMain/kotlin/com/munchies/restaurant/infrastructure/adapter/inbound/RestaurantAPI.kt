package com.munchies.restaurant.infrastructure.adapter.inbound

import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.CreateRestaurantRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.UpdateRestaurantRequest
import kotlin.js.JsExport

@JsExport
object RestaurantAPI {

  interface CreateRestaurantAPI<Response> {
    fun createRestaurant(request: CreateRestaurantRequest): Response
  }

  interface GetRestaurantAPI<Response> {
    fun getRestaurant(restaurantId: String): Response
  }

  interface GetManagerRestaurantsAPI<Response> {
    fun getManagerRestaurants(managerId: String): Response
  }

  interface UpdateRestaurantAPI<Response> {
    fun updateRestaurant(request: UpdateRestaurantRequest): Response
  }

  interface DeleteRestaurantAPI<Response> {
    fun deleteRestaurant(managerId: String, restaurantId: String): Response
  }
}
