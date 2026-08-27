package com.munchies.restaurant.infrastructure.adapter.dto

import com.munchies.commons.infrastructure.adapter.wireJson
import kotlin.js.JsExport
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

@JsExport
@Serializable
class RestaurantDto(
  val id: String,
  val name: String,
  val address: String,
  val phone: String,
  val email: String,
) {
  fun toJson(): String = wireJson.encodeToString(this)
}
