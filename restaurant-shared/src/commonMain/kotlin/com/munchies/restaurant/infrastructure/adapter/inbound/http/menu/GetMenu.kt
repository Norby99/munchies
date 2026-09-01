package com.munchies.restaurant.infrastructure.adapter.inbound.http.menu

import com.munchies.commons.infrastructure.adapter.WebResponse
import com.munchies.commons.infrastructure.adapter.wireJson
import com.munchies.restaurant.infrastructure.adapter.dto.MenuDto
import com.munchies.restaurant.infrastructure.adapter.dto.MenuSummaryDto
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
@SerialName("GetMenuResponse")
class GetMenuResponse(
  override val result: MenuDto,
  override val code: Int = 200,
) : WebResponse<MenuDto>() {
  override fun toJson(): String = wireJson.encodeToString(this)
}

@JsExport
fun getMenuResponseFromJson(json: String): GetMenuResponse = Json.decodeFromString(json)

@JsExport
@Serializable
@SerialName("GetRestaurantMenusResponse")
class GetRestaurantMenusResponse(
  override val result: Array<MenuSummaryDto> = emptyArray(),
  override val code: Int = 200,
) : WebResponse<Array<MenuSummaryDto>>() {
  override fun toJson(): String = wireJson.encodeToString(this)
}

@JsExport
fun getRestaurantMenusResponseFromJson(json: String): GetRestaurantMenusResponse =
  Json.decodeFromString(json)
