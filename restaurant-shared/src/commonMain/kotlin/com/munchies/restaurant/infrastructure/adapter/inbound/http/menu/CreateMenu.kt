package com.munchies.restaurant.infrastructure.adapter.inbound.http.menu

import com.munchies.commons.infrastructure.adapter.AuthenticatedRequest
import com.munchies.commons.infrastructure.adapter.JsonEncodable
import com.munchies.commons.infrastructure.adapter.WebResponse
import com.munchies.commons.infrastructure.adapter.wireJson
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
  val managerId: String = "",
  val name: String,
) : AuthenticatedRequest<CreateMenuRequest>, JsonEncodable() {
  override fun toJson(): String = wireJson.encodeToString(this)
  override fun addId(userId: String): CreateMenuRequest = copy(managerId = userId)
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
  override fun toJson(): String = wireJson.encodeToString(this)
}

@JsExport
fun createMenuResponseFromJson(json: String): CreateMenuResponse = Json.decodeFromString(json)
