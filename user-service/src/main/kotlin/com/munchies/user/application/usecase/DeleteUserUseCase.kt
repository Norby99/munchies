package com.munchies.user.application.usecase

import com.munchies.user.application.port.inbound.DeleteUser
import com.munchies.user.application.port.inbound.DeleteUser.Companion.DeleteUserResult
import com.munchies.user.domain.model.UserId
import com.munchies.user.domain.port.UserRepository
import org.slf4j.LoggerFactory

/**
 * Deletes an existing user aggregate when the provided identifier can be resolved.
 *
 * If the user does not exist, the use case returns the corresponding not-found result.
 */
class DeleteUserUseCase(private val repository: UserRepository) : DeleteUser {
  override fun execute(id: UserId): DeleteUserResult = repository.findById(id)?.let { user ->
    repository.delete(user)
    logger.info("User {} deleted", user.id.value)
    return DeleteUserResult.Success(user)
  } ?: DeleteUserResult.NotFound

  private companion object {
    val logger = LoggerFactory.getLogger(DeleteUserUseCase::class.java)
  }
}
