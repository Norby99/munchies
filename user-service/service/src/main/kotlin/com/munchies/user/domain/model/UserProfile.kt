package com.munchies.user.domain.model

data class UserProfile(
  val username: String,
  val email: String,
  val role: UserRole,
) {
  companion object {
    val empty: UserProfile = UserProfile(
      username = "",
      email = "",
      role = UserRole.CUSTOMER,
    )
  }
}
