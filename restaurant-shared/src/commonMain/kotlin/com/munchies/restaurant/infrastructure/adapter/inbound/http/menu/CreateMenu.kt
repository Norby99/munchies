package com.munchies.restaurant.infrastructure.adapter.inbound.http.menu

import com.munchies.restaurant.infrastructure.adapter.dto.MenuDto
import kotlin.js.JsExport
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
class CreateMenuRequest(
  val name: String,
) {
  fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun createMenuRequestFromJson(json: String): CreateMenuRequest = Json.decodeFromString(json)

@JsExport
@Serializable
class CreateMenuResponse(
  val menu: MenuDto,
) {
  fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun createMenuResponseFromJson(json: String): CreateMenuResponse = Json.decodeFromString(json)
