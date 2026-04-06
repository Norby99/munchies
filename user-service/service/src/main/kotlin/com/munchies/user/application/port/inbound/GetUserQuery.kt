package com.munchies.user.application.port.inbound

import com.munchies.user.domain.model.User
import com.munchies.user.domain.model.UserId

interface GetUserQuery {

  fun execute(id: UserId): GetUserResult

  companion object {
    sealed interface GetUserResult {
      data class Success(val user: User) : GetUserResult
      object NotFound : GetUserResult
    }
  }
}
