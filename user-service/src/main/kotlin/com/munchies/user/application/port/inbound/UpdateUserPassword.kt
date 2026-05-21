package com.munchies.user.application.port.inbound

import com.munchies.user.domain.model.User

interface UpdateUserPassword {
  fun execute(user: User, oldPassword: String, newPassword: String): UpdateUserPasswordResult

  companion object {
    sealed interface UpdateUserPasswordResult {
      data object Success : UpdateUserPasswordResult
      data object LockedUser : UpdateUserPasswordResult
      data object WrongCredentials : UpdateUserPasswordResult
      data object UserNotFound : UpdateUserPasswordResult
    }
  }
}
