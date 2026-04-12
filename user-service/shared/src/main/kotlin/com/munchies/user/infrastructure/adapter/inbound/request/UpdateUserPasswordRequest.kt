package com.munchies.user.infrastructure.adapter.inbound.request

import com.munchies.user.infrastructure.adapter.dto.UserDTO

data class UpdateUserPasswordRequest(
  val user: UserDTO,
  val oldHashedPassword: String,
  val newPassword: String,
)
