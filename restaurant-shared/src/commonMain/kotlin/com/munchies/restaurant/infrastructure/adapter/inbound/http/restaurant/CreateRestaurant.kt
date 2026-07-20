package com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant

import kotlin.js.JsExport
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
class CreateRestaurantRequest(
  val managerId: String,
  val name: String,
  val address: String,
  val phone: String,
  val email: String,
) {
  fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun createRestaurantRequestFromJson(json: String): CreateRestaurantRequest =
  Json.decodeFromString(json)

@JsExport
@Serializable
class CreateRestaurantResponse(
  val restaurantId: String,
) {
  fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun createRestaurantResponseFromJson(json: String): CreateRestaurantResponse =
  Json.decodeFromString(json)
