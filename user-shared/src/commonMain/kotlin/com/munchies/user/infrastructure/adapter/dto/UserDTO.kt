package com.munchies.user.infrastructure.adapter.dto

import kotlin.js.JsExport
import kotlinx.serialization.Serializable

@JsExport
@Serializable
data class UserDTO(
  val id: String,
  val username: String,
  val email: String,
  val role: String,
)
