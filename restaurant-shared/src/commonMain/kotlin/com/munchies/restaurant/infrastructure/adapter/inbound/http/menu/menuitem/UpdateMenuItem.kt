package com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem

import com.munchies.restaurant.infrastructure.adapter.dto.MenuItemDto
import com.munchies.restaurant.infrastructure.adapter.dto.VariationDto
import kotlin.js.JsExport
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
class UpdateMenuItemRequest(
  val name: String,
  val description: String,
  val price: String,
  val variations: Array<VariationDto> = emptyArray(),
) {
  fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun updateMenuItemRequestFromJson(json: String): UpdateMenuItemRequest = Json.decodeFromString(json)

@JsExport
@Serializable
class UpdateMenuItemResponse(
  val menuItem: MenuItemDto,
) {
  fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun updateMenuItemResponseFromJson(json: String): UpdateMenuItemResponse =
  Json.decodeFromString(json)
