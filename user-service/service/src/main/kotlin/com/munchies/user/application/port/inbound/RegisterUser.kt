package com.munchies.user.application.port.inbound

import com.munchies.user.domain.model.User
import com.munchies.user.domain.model.UserCredentials

interface RegisterUser {
  fun execute(user: User, credentials: UserCredentials): RegisterUserResult

  companion object {
    sealed interface RegisterUserResult {
      data class Success(val user: User) : RegisterUserResult
      data object UserIsAlreadyRegistered : RegisterUserResult
      data class Failure(val reason: String) : RegisterUserResult
    }
  }
}
