package com.munchies.user.infrastructure.adapter.inbound.request

import com.munchies.user.infrastructure.adapter.dto.UserDTO
import kotlin.js.JsExport
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
data class RegisterUserRequest(
  val user: UserDTO,
  val hashedPassword: String,
  val saltValue: String,
) {
  fun toJson(): String = Json.encodeToString(this)
}
