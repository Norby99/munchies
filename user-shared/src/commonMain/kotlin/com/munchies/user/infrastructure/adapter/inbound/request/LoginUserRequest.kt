package com.munchies.user.infrastructure.adapter.inbound.request

import kotlin.js.JsExport
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
data class LoginUserRequest(
  val email: String,
  val username: String,
  val password: String,
) {
  fun toJson(): String = Json.encodeToString(this)
}
