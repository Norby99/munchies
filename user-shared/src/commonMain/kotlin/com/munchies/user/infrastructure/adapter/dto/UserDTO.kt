package com.munchies.user.infrastructure.adapter.dto

import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@JsExport
@Serializable
@SerialName("UserDTO")
data class UserDTO(
  val id: String,
  val username: String,
  val email: String,
  val role: String,
)
