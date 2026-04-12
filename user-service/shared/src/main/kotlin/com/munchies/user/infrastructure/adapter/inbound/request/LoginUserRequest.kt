package com.munchies.user.infrastructure.adapter.inbound.request

data class LoginUserRequest(
  val email: String,
  val username: String,
  val password: String,
)
