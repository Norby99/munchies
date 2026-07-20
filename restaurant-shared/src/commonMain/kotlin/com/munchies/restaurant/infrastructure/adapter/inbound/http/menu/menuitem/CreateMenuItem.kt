package com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem

import com.munchies.restaurant.infrastructure.adapter.dto.VariationDto
import kotlin.js.JsExport
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
class CreateMenuItemRequest(
  val name: String,
  val description: String,
  val price: String,
  val variations: List<VariationDto> = emptyList(),
) {
  fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun createMenuItemRequestFromJson(json: String): CreateMenuItemRequest = Json.decodeFromString(json)

@JsExport
@Serializable
class CreateMenuItemResponse(
  val itemId: String,
) {
  fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun createMenuItemResponseFromJson(json: String): CreateMenuItemResponse =
  Json.decodeFromString(json)
