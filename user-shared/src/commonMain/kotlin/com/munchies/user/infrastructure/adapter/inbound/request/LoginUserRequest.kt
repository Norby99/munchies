package com.munchies.user.infrastructure.adapter.inbound.request

import kotlin.js.JsExport

@JsExport
data class LoginUserRequest(
  val email: String,
  val username: String,
  val password: String,
)
