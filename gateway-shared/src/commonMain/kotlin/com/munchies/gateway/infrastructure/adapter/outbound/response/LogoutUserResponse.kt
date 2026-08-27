package com.munchies.gateway.infrastructure.adapter.outbound.response

import com.munchies.commons.infrastructure.adapter.WebResponse
import com.munchies.commons.infrastructure.adapter.wireJson
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
@SerialName("LogoutUserResponse")
open class LogoutUserResponse(
  override val result: String = "Logged out successfully",
  override val code: Int = 200,
) : WebResponse<String>() {
  override fun toJson(): String = wireJson.encodeToString(this)
}

@JsExport
fun logoutUserResponseFromJson(json: String): LogoutUserResponse = Json.decodeFromString(json)
