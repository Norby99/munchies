package com.munchies.user.application.port.inbound

import com.munchies.user.domain.model.User

/**
 * Inbound port for updating the persisted profile information of a user.
 */
interface UpdateUserInfo {

  fun execute(user: User): UpdateUserInfoResult

  companion object {
    sealed interface UpdateUserInfoResult {
      data object Success : UpdateUserInfoResult
      data object UserNotFound : UpdateUserInfoResult
      data class Failure(val reason: String = "") : UpdateUserInfoResult
    }
  }
}
