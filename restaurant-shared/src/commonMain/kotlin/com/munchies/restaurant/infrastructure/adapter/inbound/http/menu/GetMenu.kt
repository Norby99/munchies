package com.munchies.restaurant.infrastructure.adapter.inbound.http.menu

import com.munchies.commons.infrastructure.adapter.WebResponse
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
open class GetMenuResponse(
  override val result: MenuDto,
  override val code: Int = 200,
) : WebResponse<MenuDto>() {
  val menu: MenuDto get() = result
  override fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun getMenuResponseFromJson(json: String): GetMenuResponse = Json.decodeFromString(json)

@JsExport
@Serializable
@SerialName("GetRestaurantMenusResponse")
open class GetRestaurantMenusResponse(
  override val result: Array<MenuSummaryDto> = emptyArray(),
  override val code: Int = 200,
) : WebResponse<Array<MenuSummaryDto>>() {
  val menus: Array<MenuSummaryDto> get() = result
  override fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun getRestaurantMenusResponseFromJson(json: String): GetRestaurantMenusResponse =
  Json.decodeFromString(json)
