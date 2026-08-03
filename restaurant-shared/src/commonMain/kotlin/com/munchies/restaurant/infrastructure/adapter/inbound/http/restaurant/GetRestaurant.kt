package com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant

import com.munchies.commons.infrastructure.adapter.AuthenticatedRequest
import com.munchies.commons.infrastructure.adapter.JsonEncodable
import com.munchies.commons.infrastructure.adapter.WebResponse
import com.munchies.restaurant.infrastructure.adapter.dto.RestaurantDto
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
@SerialName("GetRestaurantResponse")
open class GetRestaurantResponse(
  override val result: RestaurantDto,
  override val code: Int = 200,
) : WebResponse<RestaurantDto>() {
  val restaurant: RestaurantDto get() = result
  override fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun getRestaurantResponseFromJson(json: String): GetRestaurantResponse = Json.decodeFromString(json)

@JsExport
@Serializable
@SerialName("GetManagerRestaurantsRequest")
data class GetManagerRestaurantsRequest(
  val managerId: String = "",
) : AuthenticatedRequest<GetManagerRestaurantsRequest>, JsonEncodable() {
  override fun toJson(): String = Json.encodeToString(this)
  override fun addId(userId: String): GetManagerRestaurantsRequest = copy(managerId = userId)
}

@JsExport
fun getManagerRestaurantsRequestFromJson(json: String): GetManagerRestaurantsRequest =
  Json.decodeFromString(json)

@JsExport
@Serializable
@SerialName("GetManagerRestaurantsResponse")
open class GetManagerRestaurantsResponse(
  override val result: Array<RestaurantDto>,
  override val code: Int = 200,
) : WebResponse<Array<RestaurantDto>>() {
  val restaurants: Array<RestaurantDto> get() = result
  override fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun getManagerRestaurantsResponseFromJson(json: String): GetManagerRestaurantsResponse =
  Json.decodeFromString(json)
