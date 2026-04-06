package com.munchies.user.application.port.inbound

import com.munchies.user.domain.model.UserId

interface CreateNewUser {

  fun execute(): CreateNewUserResult

  companion object {
    sealed interface CreateNewUserResult {
      data class Success(val userId: UserId) : CreateNewUserResult
    }
  }
}
