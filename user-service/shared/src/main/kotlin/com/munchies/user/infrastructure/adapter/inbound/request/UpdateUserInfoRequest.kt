package com.munchies.user.infrastructure.adapter.inbound.request

import com.munchies.user.infrastructure.adapter.dto.UserDTO

data class UpdateUserInfoRequest(
  val user: UserDTO,
) {
  companion object {
    @Throws(IllegalArgumentException::class)
    fun validate(request: UpdateUserInfoRequest) {
      require(request.user.id.isNotBlank()) { "User ID must not be blank" }
      require(request.user.username.isNotBlank()) { "Username must not be blank" }
      require(request.user.email.isNotBlank()) { "Email must not be blank" }
    }
  }
}
