package com.munchies.user.infrastructure.adapter.inbound.request

import com.munchies.user.infrastructure.adapter.dto.UserDTO
import kotlin.js.JsExport
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
data class UpdateUserInfoRequest(
  val user: UserDTO,
) {
  fun toJson(): String = Json.encodeToString(this)
}
