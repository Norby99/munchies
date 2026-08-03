package com.munchies.restaurant.infrastructure.adapter.inbound.http.menu

import com.munchies.commons.infrastructure.adapter.JsonEncodable
import com.munchies.commons.infrastructure.adapter.WebResponse
import com.munchies.restaurant.infrastructure.adapter.dto.MenuDto
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
@SerialName("CreateMenuRequest")
data class CreateMenuRequest(
  val name: String,
) : JsonEncodable() {
  override fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun createMenuRequestFromJson(json: String): CreateMenuRequest = Json.decodeFromString(json)

@JsExport
@Serializable
@SerialName("CreateMenuResponse")
open class CreateMenuResponse(
  override val result: MenuDto,
  override val code: Int = 201,
) : WebResponse<MenuDto>() {
  val menu: MenuDto get() = result
  override fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun createMenuResponseFromJson(json: String): CreateMenuResponse = Json.decodeFromString(json)
