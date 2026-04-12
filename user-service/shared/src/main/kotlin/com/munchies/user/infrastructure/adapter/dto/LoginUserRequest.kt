package com.munchies.user.infrastructure.adapter.dto

data class LoginUserRequest(
  val email: String,
  val username: String,
  val password: String,
)
