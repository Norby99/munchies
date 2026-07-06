package com.munchies.user.infrastructure.adapter.inbound.request

import kotlin.js.JsExport
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
data class UpdateUserPasswordRequest(
  val id: String,
  val username: String = "",
  val email: String = "",
  val oldHashedPassword: String,
  val newPassword: String,
) {
  fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun updateUserPasswordRequestFromJson(json: String): UpdateUserPasswordRequest =
  Json.decodeFromString(json)
