package com.munchies.user.application.usecase

import com.munchies.user.application.port.inbound.CreateNewUser
import com.munchies.user.application.port.inbound.CreateNewUser.Companion.CreateNewUserResult
import com.munchies.user.domain.port.UserRepository

/**
 * Use case implementation responsible for creating a new user in the system.
 *
 * This class belongs to the Application layer and orchestrates the user creation
 * flow by delegating persistence responsibilities to [UserRepository].
 *
 * @property repository domain port used to persist and generate a new user identifier.
 */
class CreateNewUserUseCase(private val repository: UserRepository) : CreateNewUser {
  /**
   * Executes the create-new-user use case.
   *
   * @return a [CreateNewUserResult.Success] containing the identifier of the newly created user.
   */
  override fun execute(): CreateNewUserResult = CreateNewUserResult.Success(repository.create())
}
