package com.munchies.user.infrastructure.adapter.inbound.request

import com.munchies.user.infrastructure.adapter.dto.UserDTO
import kotlin.js.JsExport

@JsExport
data class RegisterUserRequest(
  val user: UserDTO,
  val hashedPassword: String,
  val saltValue: String,
)
