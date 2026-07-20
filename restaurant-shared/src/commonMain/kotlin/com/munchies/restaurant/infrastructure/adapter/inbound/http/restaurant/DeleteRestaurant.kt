package com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant

import kotlin.js.JsExport
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
class DeleteRestaurantRequest(
  val managerId: String,
) {
  fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun deleteRestaurantRequestFromJson(json: String): DeleteRestaurantRequest =
  Json.decodeFromString(json)

@JsExport
@Serializable
class DeleteRestaurantResponse(
  val restaurantId: String,
) {
  fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun deleteRestaurantResponseFromJson(json: String): DeleteRestaurantResponse =
  Json.decodeFromString(json)
