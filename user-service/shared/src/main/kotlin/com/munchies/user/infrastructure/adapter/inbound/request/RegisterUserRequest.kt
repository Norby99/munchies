package com.munchies.user.infrastructure.adapter.inbound.request

import com.munchies.user.infrastructure.adapter.dto.UserDTO

data class RegisterUserRequest(
  val user: UserDTO,
  val hashedPassword: String,
  val saltValue: String,
)
