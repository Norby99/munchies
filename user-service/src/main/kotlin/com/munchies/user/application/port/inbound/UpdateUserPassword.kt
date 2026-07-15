package com.munchies.user.application.port.inbound

/**
 * Inbound port for changing a user's password after validating the current credentials.
 */
interface UpdateUserPassword {
  fun execute(
    id: String,
    username: String,
    email: String,
    oldPassword: String,
    newPassword: String,
  ): UpdateUserPasswordResult

  companion object {
    sealed interface UpdateUserPasswordResult {
      data object Success : UpdateUserPasswordResult
      data object LockedUser : UpdateUserPasswordResult
      data object WrongCredentials : UpdateUserPasswordResult
      data object UserNotFound : UpdateUserPasswordResult
      data object UnauthorizedOperation : UpdateUserPasswordResult
    }
  }
}
