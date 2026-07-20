package com.munchies.restaurant.infrastructure.adapter.inbound.http.menu

import kotlin.js.JsExport
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
class DeleteMenuResponse(
  val menuId: String,
) {
  fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun deleteMenuResponseFromJson(json: String): DeleteMenuResponse = Json.decodeFromString(json)
