package com.munchies.user.application.usecase

import com.munchies.user.application.port.inbound.GetUser
import com.munchies.user.application.port.inbound.GetUser.Companion.GetUserResult
import com.munchies.user.application.port.inbound.GetUser.Companion.GetUserResult.NotFound
import com.munchies.user.application.port.inbound.GetUser.Companion.GetUserResult.Success
import com.munchies.user.domain.model.UserId
import com.munchies.user.domain.port.UserRepository

/**
 * Application use case for retrieving a user by identifier.
 *
 * This class implements the inbound query port and delegates data access to the
 * [UserRepository] domain port, mapping repository outcomes to [GetUserResult].
 *
 * @property repository repository used to lookup users by [UserId].
 */
class GetUserUseCase(private val repository: UserRepository) : GetUser {
  /**
   * Executes the user lookup use case.
   *
   * @param id unique domain identifier of the user to retrieve.
   * @return [Success] when a user is found, or [NotFound] when no user exists for the given [id].
   */
  override fun execute(id: UserId): GetUserResult {
    val user = repository.findById(id)
    return user?.let { Success(it) } ?: NotFound
  }
}
