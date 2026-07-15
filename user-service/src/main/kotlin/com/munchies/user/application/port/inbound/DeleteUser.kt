package com.munchies.user.application.port.inbound

import com.munchies.user.domain.model.User
import com.munchies.user.domain.model.UserId

/**
 * Inbound port for deleting a user by identifier.
 *
 * Implementations remove the matching aggregate from persistence and report whether the user existed.
 */
interface DeleteUser {

  fun execute(id: UserId): DeleteUserResult

  companion object {
    sealed interface DeleteUserResult {
      data class Success(val user: User) : DeleteUserResult
      object NotFound : DeleteUserResult
    }
  }
}
