package com.munchies.user.application.usecase

import com.munchies.user.application.port.inbound.DeleteUser
import com.munchies.user.application.port.inbound.DeleteUser.Companion.DeleteUserResult
import com.munchies.user.domain.model.UserId
import com.munchies.user.domain.port.UserRepository

class DeleteUserUseCase(private val repository: UserRepository) : DeleteUser {
  override fun execute(id: UserId): DeleteUserResult {
    repository.findById(id)?.let { user ->
      repository.delete(user)
      return DeleteUserResult.Success(user)
    }
    return DeleteUserResult.NotFound
  }
}
