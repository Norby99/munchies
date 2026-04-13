package com.munchies.user.infrastructure.adapter.inbound.request

import com.munchies.user.infrastructure.adapter.dto.UserDTO

data class UpdateUserPasswordRequest(
  val user: UserDTO,
  val oldHashedPassword: String,
  val newPassword: String,
) {
  companion object {
    @Throws(IllegalArgumentException::class)
    fun validate(request: UpdateUserPasswordRequest) {
      require(
        request.user.username.isNotBlank() ||
          request.user.email.isNotBlank(),
      ) { "User identifications must not be blank" }
      require(request.oldHashedPassword.isNotBlank()) { "Old hashed password must not be blank" }
      require(request.newPassword.isNotBlank()) { "New password must not be blank" }
    }
  }
}
