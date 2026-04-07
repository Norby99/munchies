package com.munchies.user.domain.model

enum class UserRole {
  CUSTOMER,
  MANAGER,
  ;

  companion object {
    fun String.toUserRole(): UserRole = when (this.uppercase()) {
      "MANAGER" -> MANAGER
      else -> CUSTOMER
    }
  }
}
