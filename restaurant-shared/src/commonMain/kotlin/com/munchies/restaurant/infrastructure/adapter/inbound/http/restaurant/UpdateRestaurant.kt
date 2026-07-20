package com.munchies.restaurant.infrastructure.adapter.inbound.http.restaurant

import kotlin.js.JsExport
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
class UpdateRestaurantRequest(
  val managerId: String,
  val name: String,
  val address: String,
  val phone: String,
  val email: String,
) {
  fun toJson(): String = Json.encodeToString(this)
}

@JsExport
@Serializable
class UpdateRestaurantResponse(
  val restaurantId: String,
) {
  fun toJson(): String = Json.encodeToString(this)
}
