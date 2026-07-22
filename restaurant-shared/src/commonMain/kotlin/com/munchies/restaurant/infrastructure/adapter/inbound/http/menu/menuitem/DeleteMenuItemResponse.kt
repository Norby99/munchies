package com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem

import kotlin.js.JsExport
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
class DeleteMenuItemResponse(
  val menuItemId: String,
) {
  fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun deleteMenuItemResponseFromJson(json: String): DeleteMenuItemResponse = Json.decodeFromString(
  json,
)
