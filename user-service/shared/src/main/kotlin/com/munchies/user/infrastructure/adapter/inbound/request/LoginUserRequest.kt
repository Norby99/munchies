package com.munchies.user.infrastructure.adapter.inbound.request

data class LoginUserRequest(
  val email: String,
  val username: String,
  val password: String,
) {
  companion object {
    @Throws(IllegalArgumentException::class)
    fun validate(request: LoginUserRequest) {
      require(
        request.email.isNotBlank() ||
          request.username.isNotEmpty(),
      ) { "Email or Username must not be blank" }
      require(request.password.isNotBlank()) { "Password must not be blank" }
    }
  }
}
