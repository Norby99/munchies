package com.munchies.restaurant.infrastructure.adapter.inbound.http.menu

import com.munchies.restaurant.infrastructure.adapter.dto.MenuDto
import com.munchies.restaurant.infrastructure.adapter.dto.ValidityDto
import kotlin.js.JsExport
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
class UpdateMenuRequest(
  val name: String,
  val validity: ValidityDto,
) {
  fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun updateMenuRequestFromJson(json: String): UpdateMenuRequest = Json.decodeFromString(json)

@JsExport
@Serializable
class UpdateMenuResponse(
  val menu: MenuDto,
) {
  fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun updateMenuResponseFromJson(json: String): UpdateMenuResponse = Json.decodeFromString(json)
