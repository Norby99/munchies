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

/**
 * Customer search of restaurants by name or address. The search text is the `q` query parameter
 * ([RestaurantServiceConfig.SEARCH_QUERY_PARAM]); an empty one lists every restaurant.
 */
@JsExport
abstract class JsSearchRestaurantsAPI<E : WebResponse<Any>> :
  SimpleAPI<Nothing, SearchRestaurantsResponse>() {
  abstract fun searchRestaurants(query: String): Promise<E>
  override fun parseResponse(json: String): SearchRestaurantsResponse =
    searchRestaurantsResponseFromJson(json)
  override fun parseError(json: String): ErrorResponse = errorResponseFromJson(json)
  override fun getPath(): String =
    RestaurantServiceConfig.SERVICE_PATH + RestaurantServiceConfig.SEARCH_RESTAURANTS_PATH
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

/**
 * Reading the capacity policy is open to customers: its opening and closing times are the
 * restaurant's hours, shown on the restaurant page. Changing it stays manager-only
 * ([JsUpdateCapacityPolicyAPI]).
 */
@JsExport
abstract class JsGetCapacityPolicyAPI<E : WebResponse<Any>> :
  SimpleAPI<Nothing, GetCapacityPolicyResponse>() {
  abstract fun getCapacityPolicy(restaurantId: String): Promise<E>
  override fun parseResponse(json: String): GetCapacityPolicyResponse =
    getCapacityPolicyResponseFromJson(json)
  override fun parseError(json: String): ErrorResponse = errorResponseFromJson(json)
  override fun getPath(): String =
    RestaurantServiceConfig.SERVICE_PATH + RestaurantServiceConfig.CAPACITY_POLICY_PATH
  override fun getPort(): Int = RestaurantServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.GET
  override fun getRequiredAuthRole(): AuthRole = AuthRole.CUSTOMER
  override fun parseRequest(json: String): Nothing = throw UnsupportedOperationException()
}

@JsExport
abstract class JsUpdateCapacityPolicyAPI<E : WebResponse<Any>> :
  SimpleAPI<UpdateCapacityPolicyRequest, UpdateCapacityPolicyResponse>() {
  abstract fun updateCapacityPolicy(
    restaurantId: String,
    request: UpdateCapacityPolicyRequest,
  ): Promise<E>
  override fun parseRequest(json: String): UpdateCapacityPolicyRequest =
    updateCapacityPolicyRequestFromJson(json)
  override fun parseResponse(json: String): UpdateCapacityPolicyResponse =
    updateCapacityPolicyResponseFromJson(json)
  override fun parseError(json: String): ErrorResponse = errorResponseFromJson(json)
  override fun getPath(): String =
    RestaurantServiceConfig.SERVICE_PATH + RestaurantServiceConfig.CAPACITY_POLICY_PATH
  override fun getPort(): Int = RestaurantServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.PUT
  override fun getRequiredAuthRole(): AuthRole = AuthRole.MANAGER
}

@JsExport
abstract class JsGetRestaurantLocationAPI<E : WebResponse<Any>> :
  SimpleAPI<Nothing, GetRestaurantLocationResponse>() {
  abstract fun getRestaurantLocation(restaurantId: String): Promise<E>
  override fun parseResponse(json: String): GetRestaurantLocationResponse =
    getRestaurantLocationResponseFromJson(json)
  override fun parseError(json: String): ErrorResponse = errorResponseFromJson(json)
  override fun getPath(): String =
    RestaurantServiceConfig.SERVICE_PATH + RestaurantServiceConfig.RESTAURANT_LOCATION_PATH
  override fun getPort(): Int = RestaurantServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.GET
  override fun getRequiredAuthRole(): AuthRole = AuthRole.MANAGER
  override fun parseRequest(json: String): Nothing = throw UnsupportedOperationException()
}

@JsExport
abstract class JsUpdateRestaurantLocationAPI<E : WebResponse<Any>> :
  SimpleAPI<UpdateRestaurantLocationRequest, UpdateRestaurantLocationResponse>() {
  abstract fun updateRestaurantLocation(
    restaurantId: String,
    request: UpdateRestaurantLocationRequest,
  ): Promise<E>
  override fun parseRequest(json: String): UpdateRestaurantLocationRequest =
    updateRestaurantLocationRequestFromJson(json)
  override fun parseResponse(json: String): UpdateRestaurantLocationResponse =
    updateRestaurantLocationResponseFromJson(json)
  override fun parseError(json: String): ErrorResponse = errorResponseFromJson(json)
  override fun getPath(): String =
    RestaurantServiceConfig.SERVICE_PATH + RestaurantServiceConfig.RESTAURANT_LOCATION_PATH
  override fun getPort(): Int = RestaurantServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.PUT
  override fun getRequiredAuthRole(): AuthRole = AuthRole.MANAGER
}

/**
 * Reading the tables is public within the authenticated area: customers need them to see how a
 * restaurant can seat them, managers to edit the layout.
 */
@JsExport
abstract class JsGetTablesAPI<E : WebResponse<Any>> :
  SimpleAPI<Nothing, GetTablesResponse>() {
  abstract fun getTables(restaurantId: String): Promise<E>
  override fun parseResponse(json: String): GetTablesResponse = getTablesResponseFromJson(json)
  override fun parseError(json: String): ErrorResponse = errorResponseFromJson(json)
  override fun getPath(): String =
    RestaurantServiceConfig.SERVICE_PATH + RestaurantServiceConfig.TABLES_PATH
  override fun getPort(): Int = RestaurantServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.GET
  override fun getRequiredAuthRole(): AuthRole = AuthRole.CUSTOMER
  override fun parseRequest(json: String): Nothing = throw UnsupportedOperationException()
}

@JsExport
abstract class JsUpdateTablesAPI<E : WebResponse<Any>> :
  SimpleAPI<UpdateTablesRequest, UpdateTablesResponse>() {
  abstract fun updateTables(restaurantId: String, request: UpdateTablesRequest): Promise<E>
  override fun parseRequest(json: String): UpdateTablesRequest = updateTablesRequestFromJson(json)
  override fun parseResponse(json: String): UpdateTablesResponse =
    updateTablesResponseFromJson(json)
  override fun parseError(json: String): ErrorResponse = errorResponseFromJson(json)
  override fun getPath(): String =
    RestaurantServiceConfig.SERVICE_PATH + RestaurantServiceConfig.TABLES_PATH
  override fun getPort(): Int = RestaurantServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.PUT
  override fun getRequiredAuthRole(): AuthRole = AuthRole.MANAGER
}
