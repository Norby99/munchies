package com.munchies.user.infrastructure.adapter.inbound.request

import com.munchies.commons.infrastructure.adapter.JsonEncodable
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
@SerialName("RegisterUserRequest")
data class RegisterUserRequest(
  val username: String,
  val email: String,
  val role: String,
  val hashedPassword: String,
  val saltValue: String,
) : JsonEncodable() {
  override fun toJson(): String = Json.encodeToString(this as RegisterUserRequest)
}

@JsExport
fun registerUserRequestFromJson(json: String): RegisterUserRequest =
  (Json.decodeFromString(json) as RegisterUserRequest)
