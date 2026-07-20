package com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant

import com.munchies.restaurant.infrastructure.adapter.dto.RestaurantDto
import kotlin.js.JsExport
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
class GetRestaurantResponse(
  val restaurant: RestaurantDto,
) {
  fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun getRestaurantResponseFromJson(json: String): GetRestaurantResponse = Json.decodeFromString(json)

@JsExport
@Serializable
class GetManagerRestaurantsRequest(
  val managerId: String,
) {
  fun toJson(): String = Json.encodeToString(this)
}

@JsExport
@Serializable
class GetManagerRestaurantsResponse(
  val restaurants: List<RestaurantDto>,
) {
  fun toJson(): String = Json.encodeToString(this)
}
