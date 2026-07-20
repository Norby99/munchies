package com.munchies.restaurant.infrastructure.adapter.inbound

import com.munchies.commons.infrastructure.adapter.API
import com.munchies.commons.infrastructure.adapter.HttpMethod
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.CreateRestaurantRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.CreateRestaurantResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.DeleteRestaurantResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.GetManagerRestaurantsResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.GetRestaurantResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.UpdateRestaurantRequest
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.UpdateRestaurantResponse
import com.munchies.restaurant.infrastructure.adapter.inbound.web.config.RestaurantServiceConfig
import kotlin.js.Promise

@JsExport
abstract class JsCreateRestaurantAPI :
  RestaurantAPI.CreateRestaurantAPI<Promise<CreateRestaurantResponse>>, API() {
  override fun getPath(): String =
    RestaurantServiceConfig.SERVICE_PATH + RestaurantServiceConfig.CREATE_RESTAURANT_PATH
  override fun getPort(): Int = RestaurantServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.POST
  abstract override fun createRestaurant(
    request: CreateRestaurantRequest,
  ): Promise<CreateRestaurantResponse>
}

@JsExport
abstract class JsGetRestaurantAPI :
  RestaurantAPI.GetRestaurantAPI<Promise<GetRestaurantResponse>>, API() {
  override fun getPath(): String =
    RestaurantServiceConfig.SERVICE_PATH + RestaurantServiceConfig.GET_RESTAURANT_PATH
  override fun getPort(): Int = RestaurantServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.GET
  abstract override fun getRestaurant(restaurantId: String): Promise<GetRestaurantResponse>
}

@JsExport
abstract class JsGetManagerRestaurantsAPI :
  RestaurantAPI.GetManagerRestaurantsAPI<Promise<GetManagerRestaurantsResponse>>, API() {
  override fun getPath(): String =
    RestaurantServiceConfig.SERVICE_PATH + RestaurantServiceConfig.GET_MANAGER_RESTAURANTS_PATH
  override fun getPort(): Int = RestaurantServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.GET
  abstract override fun getManagerRestaurants(
    managerId: String,
  ): Promise<GetManagerRestaurantsResponse>
}

@JsExport
abstract class JsUpdateRestaurantAPI :
  RestaurantAPI.UpdateRestaurantAPI<Promise<UpdateRestaurantResponse>>, API() {
  override fun getPath(): String =
    RestaurantServiceConfig.SERVICE_PATH + RestaurantServiceConfig.UPDATE_RESTAURANT_PATH
  override fun getPort(): Int = RestaurantServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.PUT
  abstract override fun updateRestaurant(
    request: UpdateRestaurantRequest,
  ): Promise<UpdateRestaurantResponse>
}

@JsExport
abstract class JsDeleteRestaurantAPI :
  RestaurantAPI.DeleteRestaurantAPI<Promise<DeleteRestaurantResponse>>, API() {
  override fun getPath(): String =
    RestaurantServiceConfig.SERVICE_PATH + RestaurantServiceConfig.DELETE_RESTAURANT_PATH
  override fun getPort(): Int = RestaurantServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.DELETE
  abstract override fun deleteRestaurant(
    managerId: String,
    restaurantId: String,
  ): Promise<DeleteRestaurantResponse>
}
