package com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem

import com.munchies.commons.infrastructure.adapter.JsonEncodable
import com.munchies.commons.infrastructure.adapter.WebResponse
import com.munchies.restaurant.infrastructure.adapter.dto.MenuItemDto
import com.munchies.restaurant.infrastructure.adapter.dto.VariationDto
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
@SerialName("UpdateMenuItemRequest")
data class UpdateMenuItemRequest(
  val name: String,
  val description: String,
  val price: String,
  val variations: Array<VariationDto> = emptyArray(),
) : JsonEncodable() {
  override fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun updateMenuItemRequestFromJson(json: String): UpdateMenuItemRequest = Json.decodeFromString(json)

@JsExport
@Serializable
@SerialName("UpdateMenuItemResponse")
open class UpdateMenuItemResponse(
  override val result: MenuItemDto,
  override val code: Int = 200,
) : WebResponse<MenuItemDto>() {
  val menuItem: MenuItemDto get() = result
  override fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun updateMenuItemResponseFromJson(json: String): UpdateMenuItemResponse =
  Json.decodeFromString(json)
