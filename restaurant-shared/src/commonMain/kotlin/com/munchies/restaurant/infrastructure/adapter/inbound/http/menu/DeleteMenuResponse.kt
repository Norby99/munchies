package com.munchies.restaurant.infrastructure.adapter.inbound.http.menu

import com.munchies.commons.infrastructure.adapter.AuthenticatedRequest
import com.munchies.commons.infrastructure.adapter.JsonEncodable
import com.munchies.commons.infrastructure.adapter.WebResponse
import com.munchies.commons.infrastructure.adapter.wireJson
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
@SerialName("DeleteMenuRequest")
data class DeleteMenuRequest(
  val managerId: String = "",
) : AuthenticatedRequest<DeleteMenuRequest>, JsonEncodable() {
  override fun toJson(): String = wireJson.encodeToString(this)
  override fun addId(userId: String): DeleteMenuRequest = copy(managerId = userId)
}

@JsExport
fun deleteMenuRequestFromJson(json: String): DeleteMenuRequest = Json.decodeFromString(json)

@JsExport
@Serializable
@SerialName("DeleteMenuResponse")
class DeleteMenuResponse(
  override val result: String,
  override val code: Int = 200,
) : WebResponse<String>() {
  override fun toJson(): String = wireJson.encodeToString(this)
}

@JsExport
fun deleteMenuResponseFromJson(json: String): DeleteMenuResponse = Json.decodeFromString(json)
