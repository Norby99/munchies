package com.munchies.restaurant.infrastructure.adapter.inbound.http.menu

import com.munchies.restaurant.infrastructure.adapter.dto.MenuDto
import com.munchies.restaurant.infrastructure.adapter.dto.MenuSummaryDto
import kotlin.js.JsExport
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
class GetMenuResponse(
  val menu: MenuDto,
) {
  fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun getMenuResponseFromJson(json: String): GetMenuResponse = Json.decodeFromString(json)

@JsExport
@Serializable
class GetRestaurantMenusResponse(
  val menus: List<MenuSummaryDto>,
) {
  fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun getRestaurantMenusResponseFromJson(json: String): GetRestaurantMenusResponse =
  Json.decodeFromString(json)
