package com.munchies.user.infrastructure.adapter.inbound.request

import kotlin.js.JsExport

@JsExport
data class UpdateUserPasswordRequest(
  val id: String,
  val username: String = "",
  val email: String = "",
  val oldHashedPassword: String,
  val newPassword: String,
)
