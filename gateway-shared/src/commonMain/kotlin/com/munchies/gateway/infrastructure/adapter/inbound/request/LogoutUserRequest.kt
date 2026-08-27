package com.munchies.gateway.infrastructure.adapter.inbound.request

import com.munchies.commons.infrastructure.adapter.AuthenticatedRequest
import com.munchies.commons.infrastructure.adapter.JsonEncodable
import com.munchies.commons.infrastructure.adapter.wireJson
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
@SerialName("LogoutUserRequest")
data class LogoutUserRequest(
  val userId: String = "",
) : AuthenticatedRequest<LogoutUserRequest>, JsonEncodable() {
  override fun toJson(): String = wireJson.encodeToString(this)
  override fun addId(userId: String): LogoutUserRequest = this.copy(userId = userId)
}

@JsExport
fun logoutUserRequestFromJson(json: String): LogoutUserRequest = Json.decodeFromString(json)
