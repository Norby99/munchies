package com.munchies.restaurant.infrastructure.adapter.inbound.http.menu.menuitem

import com.munchies.commons.infrastructure.adapter.JsonEncodable
import com.munchies.commons.infrastructure.adapter.WebResponse
import com.munchies.restaurant.infrastructure.adapter.dto.VariationDto
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
@SerialName("CreateMenuItemRequest")
data class CreateMenuItemRequest(
  val name: String,
  val description: String,
  val price: String,
  val variations: Array<VariationDto> = emptyArray(),
) : JsonEncodable() {
  override fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun createMenuItemRequestFromJson(json: String): CreateMenuItemRequest = Json.decodeFromString(json)

@JsExport
@Serializable
@SerialName("CreateMenuItemResponse")
open class CreateMenuItemResponse(
  override val result: String,
  override val code: Int = 201,
) : WebResponse<String>() {
  val itemId: String get() = result
  override fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun createMenuItemResponseFromJson(json: String): CreateMenuItemResponse =
  Json.decodeFromString(json)
