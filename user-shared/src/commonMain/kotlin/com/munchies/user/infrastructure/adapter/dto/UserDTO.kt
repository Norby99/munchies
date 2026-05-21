package com.munchies.user.infrastructure.adapter.dto

import kotlin.js.JsExport

@JsExport
data class UserDTO(
  val id: String,
  val username: String,
  val email: String,
  val role: String,
)
