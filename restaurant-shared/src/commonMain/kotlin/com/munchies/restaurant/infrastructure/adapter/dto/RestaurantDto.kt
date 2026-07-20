package com.munchies.restaurant.infrastructure.adapter.dto

import kotlin.js.JsExport
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
class RestaurantDto(
  val id: String,
  val name: String,
  val address: String,
  val phone: String,
  val email: String,
) {
  fun toJson(): String = Json.encodeToString(this)
}
