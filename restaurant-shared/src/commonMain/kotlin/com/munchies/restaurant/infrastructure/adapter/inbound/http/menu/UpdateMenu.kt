package com.munchies.restaurant.infrastructure.adapter.inbound.http.menu

import com.munchies.commons.infrastructure.adapter.JsonEncodable
import com.munchies.commons.infrastructure.adapter.WebResponse
import com.munchies.restaurant.infrastructure.adapter.dto.MenuDto
import com.munchies.restaurant.infrastructure.adapter.dto.ValidityDto
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
@SerialName("UpdateMenuRequest")
class UpdateMenuRequest(
  val name: String,
  val validity: Array<ValidityDto>,
) : JsonEncodable() {
  override fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun updateMenuRequestFromJson(json: String): UpdateMenuRequest = Json.decodeFromString(json)

@JsExport
@Serializable
@SerialName("UpdateMenuResponse")
open class UpdateMenuResponse(
  override val result: MenuDto,
  override val code: Int = 200,
) : WebResponse<MenuDto>() {
  override fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun updateMenuResponseFromJson(json: String): UpdateMenuResponse = Json.decodeFromString(json)
