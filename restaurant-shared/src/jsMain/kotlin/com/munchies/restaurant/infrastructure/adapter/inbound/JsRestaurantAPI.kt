package com.munchies.restaurant.infrastructure.adapter.inbound

import com.munchies.commons.domain.port.AuthRole
import com.munchies.commons.infrastructure.adapter.ErrorResponse
import com.munchies.commons.infrastructure.adapter.HttpMethod
import com.munchies.commons.infrastructure.adapter.SimpleAPI
import com.munchies.commons.infrastructure.adapter.WebResponse
import com.munchies.commons.infrastructure.adapter.errorResponseFromJson
import com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant.*
import com.munchies.restaurant.infrastructure.adapter.inbound.web.config.RestaurantServiceConfig
import kotlin.js.JsExport
import kotlin.js.Promise

@JsExport
abstract class JsCreateRestaurantAPI<E : WebResponse<Any>> :
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

  abstract fun createRestaurant(request: CreateRestaurantRequest): Promise<E>
}

@JsExport
abstract class JsGetRestaurantAPI<E : WebResponse<Any>> :
  SimpleAPI<Nothing, GetRestaurantResponse>() {
  abstract fun getRestaurant(restaurantId: String): Promise<E>
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
abstract class JsGetManagerRestaurantsAPI<E : WebResponse<Any>> :
  SimpleAPI<Nothing, GetManagerRestaurantsResponse>() {
  abstract fun getManagerRestaurants(managerId: String): Promise<E>
  override fun parseResponse(json: String): GetManagerRestaurantsResponse =
    getManagerRestaurantsResponseFromJson(json)
  override fun parseError(json: String): ErrorResponse = errorResponseFromJson(json)
  override fun getPath(): String =
    RestaurantServiceConfig.SERVICE_PATH + RestaurantServiceConfig.MANAGER_RESTAURANTS_PREFIX
  override fun getPort(): Int = RestaurantServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.GET
  override fun getRequiredAuthRole(): AuthRole = AuthRole.MANAGER
  override fun parseRequest(json: String): Nothing = throw UnsupportedOperationException()
}

@JsExport
abstract class JsUpdateRestaurantAPI<E : WebResponse<Any>> :
  SimpleAPI<UpdateRestaurantRequest, UpdateRestaurantResponse>() {
  abstract fun updateRestaurant(restaurantId: String, request: UpdateRestaurantRequest): Promise<E>
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
abstract class JsDeleteRestaurantAPI<E : WebResponse<Any>> :
  SimpleAPI<DeleteRestaurantRequest, DeleteRestaurantResponse>() {
  abstract fun deleteRestaurant(managerId: String, restaurantId: String): Promise<E>
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
