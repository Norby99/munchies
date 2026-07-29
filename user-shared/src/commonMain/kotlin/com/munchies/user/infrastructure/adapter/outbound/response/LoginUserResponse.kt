package com.munchies.user.infrastructure.adapter.outbound.response

import com.munchies.commons.domain.port.AuthRole
import com.munchies.commons.infrastructure.adapter.JsonEncodable
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
@SerialName("LoginUserResponse")
class LoginUserResponse(
  val id: String,
  val role: AuthRole,
) : JsonEncodable() {
  override fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun loginUserResponseFromJson(json: String): LoginUserResponse = Json.decodeFromString(json)
