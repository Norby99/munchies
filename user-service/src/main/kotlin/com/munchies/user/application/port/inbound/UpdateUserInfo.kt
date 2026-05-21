package com.munchies.user.application.port.inbound

import com.munchies.user.domain.model.User

interface UpdateUserInfo {

  fun execute(user: User): UpdateUserInfoResult

  companion object {
    sealed interface UpdateUserInfoResult {
      data object Success : UpdateUserInfoResult
      data object UserNotFound : UpdateUserInfoResult
    }
  }
}
