package com.munchies.user.application.port.inbound

import com.munchies.user.domain.model.User
import com.munchies.user.domain.model.UserId

interface DeleteUser {

  fun execute(id: UserId): DeleteUserResult

  companion object {
    sealed interface DeleteUserResult {
      data class Success(val user: User) : DeleteUserResult
      object NotFound : DeleteUserResult
    }
  }
}
