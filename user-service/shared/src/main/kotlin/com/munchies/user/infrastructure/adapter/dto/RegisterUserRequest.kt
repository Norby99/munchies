package com.munchies.user.infrastructure.adapter.dto

data class RegisterUserRequest(
  val user: UserDTO,
  val hashedPassword: String,
  val saltValue: String,
)
