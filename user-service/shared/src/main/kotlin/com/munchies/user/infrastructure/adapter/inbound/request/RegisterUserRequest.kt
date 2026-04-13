package com.munchies.user.infrastructure.adapter.inbound.request

import com.munchies.user.infrastructure.adapter.dto.UserDTO

data class RegisterUserRequest(
  val user: UserDTO,
  val hashedPassword: String,
  val saltValue: String,
) {
  companion object {
    @Throws(IllegalArgumentException::class)
    fun validate(request: RegisterUserRequest) {
      require(request.user.username.isNotBlank()) { "Username must not be blank" }
      require(request.user.email.isNotBlank()) { "User email must not be blank" }
      require(request.hashedPassword.isNotBlank()) { "Hashed password must not be blank" }
      require(request.saltValue.isNotBlank()) { "Salt value must not be blank" }
    }
  }
}
