package com.munchies.user.infrastructure.adapter.inbound.request

import com.munchies.commons.infrastructure.adapter.JsonEncodable
import com.munchies.commons.infrastructure.adapter.wireJson
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
@SerialName("LoginUserRequest")
data class LoginUserRequest(
  val email: String,
  val username: String,
  val password: String,
) : JsonEncodable() {
  override fun toJson(): String = wireJson.encodeToString(this)
}

@JsExport
fun loginUserRequestFromJson(json: String): LoginUserRequest = Json.decodeFromString(json)
