package com.munchies.restaurant.infrastructure.adapter.inbound

import com.munchies.commons.domain.port.AuthRole
import com.munchies.commons.infrastructure.adapter.ErrorResponse
import com.munchies.commons.infrastructure.adapter.HttpMethod
import com.munchies.commons.infrastructure.adapter.SimpleAPI
import com.munchies.commons.infrastructure.adapter.errorResponseFromJson
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.*
import com.munchies.restaurant.infrastructure.adapter.inbound.web.config.RestaurantServiceConfig
import kotlin.js.Promise

@JsExport
abstract class JsCreateRestaurantAPI :
  SimpleAPI<CreateRestaurantRequest, CreateRestaurantResponse>() {
  override fun getPath(): String =
    RestaurantServiceConfig.SERVICE_PATH + RestaurantServiceConfig.CREATE_RESTAURANT_PATH
  override fun getPort(): Int = RestaurantServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.POST
  override fun getRequiredAuthRole(): AuthRole = AuthRole.MANAGER
  override fun parseRequest(json: String): CreateRestaurantRequest =
    createRestaurantRequestFromJson(json)
  override fun parseResponse(json: String): CreateRestaurantResponse =
    createRestaurantResponseFromJson(json)

  abstract fun createRestaurant(
    request: CreateRestaurantRequest,
  ): Promise<CreateRestaurantResponse>
}

@JsExport
abstract class JsGetRestaurantAPI :
  SimpleAPI<Nothing, GetRestaurantResponse>() {
  abstract fun getRestaurant(restaurantId: String): Promise<GetRestaurantResponse>
  override fun parseResponse(json: String): GetRestaurantResponse =
    getRestaurantResponseFromJson(json)
  override fun parseError(json: String): ErrorResponse = errorResponseFromJson(json)
  override fun getPath(): String =
    RestaurantServiceConfig.SERVICE_PATH + RestaurantServiceConfig.GET_RESTAURANT_PATH
  override fun getPort(): Int = RestaurantServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.GET
  override fun getRequiredAuthRole(): AuthRole = AuthRole.CUSTOMER
  override fun parseRequest(json: String): Nothing = throw UnsupportedOperationException()
}

@JsExport
abstract class JsGetManagerRestaurantsAPI :
  SimpleAPI<Nothing, GetManagerRestaurantsResponse>() {
  abstract fun getManagerRestaurants(managerId: String): Promise<GetManagerRestaurantsResponse>
  override fun parseResponse(json: String): GetManagerRestaurantsResponse =
    getManagerRestaurantsResponseFromJson(json)
  override fun parseError(json: String): ErrorResponse = errorResponseFromJson(json)
  override fun getPath(): String =
    RestaurantServiceConfig.SERVICE_PATH + RestaurantServiceConfig.GET_MANAGER_RESTAURANTS_PATH
  override fun getPort(): Int = RestaurantServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.GET
  override fun getRequiredAuthRole(): AuthRole = AuthRole.MANAGER
  override fun parseRequest(json: String): Nothing = throw UnsupportedOperationException()
}

@JsExport
abstract class JsUpdateRestaurantAPI :
  SimpleAPI<UpdateRestaurantRequest, UpdateRestaurantResponse>() {
  abstract fun updateRestaurant(request: UpdateRestaurantRequest): Promise<UpdateRestaurantResponse>
  override fun parseRequest(json: String): UpdateRestaurantRequest =
    updateRestaurantRequestFromJson(json)
  override fun parseResponse(json: String): UpdateRestaurantResponse =
    updateRestaurantResponseFromJson(json)
  override fun parseError(json: String): ErrorResponse = errorResponseFromJson(json)
  override fun getPath(): String =
    RestaurantServiceConfig.SERVICE_PATH + RestaurantServiceConfig.UPDATE_RESTAURANT_PATH
  override fun getPort(): Int = RestaurantServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.PUT
  override fun getRequiredAuthRole(): AuthRole = AuthRole.MANAGER
}

@JsExport
abstract class JsDeleteRestaurantAPI :
  SimpleAPI<DeleteRestaurantRequest, DeleteRestaurantResponse>() {
  abstract fun deleteRestaurant(
    managerId: String,
    restaurantId: String,
  ): Promise<DeleteRestaurantResponse>
  override fun parseRequest(json: String): DeleteRestaurantRequest =
    deleteRestaurantRequestFromJson(json)
  override fun parseResponse(json: String): DeleteRestaurantResponse =
    deleteRestaurantResponseFromJson(json)
  override fun parseError(json: String): ErrorResponse = errorResponseFromJson(json)
  override fun getPath(): String =
    RestaurantServiceConfig.SERVICE_PATH + RestaurantServiceConfig.DELETE_RESTAURANT_PATH
  override fun getPort(): Int = RestaurantServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.DELETE
  override fun getRequiredAuthRole(): AuthRole = AuthRole.MANAGER
}
