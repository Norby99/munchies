package com.munchies.user.application.port.inbound

import com.munchies.user.domain.model.User
import com.munchies.user.domain.model.UserCredentials

/**
 * Inbound application port for user registration.
 *
 * Implementations coordinate the registration workflow and return
 * a typed result describing the outcome.
 */
interface RegisterUser {
  /**
   * Registers a [user] using the provided [credentials].
   *
   * @param user domain user to be registered.
   * @param credentials authentication data associated with the user.
   * @return a [RegisterUserResult] indicating success or failure reason.
   */
  fun execute(user: User, credentials: UserCredentials): RegisterUserResult

  companion object {
    /**
     * Result of attempting to register a user.
     */
    sealed interface RegisterUserResult {
      /**
       * Registration completed successfully.
       *
       * @property user the registered user.
       */
      data class Success(val user: User) : RegisterUserResult

      /**
       * Registration was rejected because the user is already registered.
       */
      data object UserIsAlreadyRegistered : RegisterUserResult

      /**
       * Registration failed due to an application or infrastructure error.
       *
       * @property reason human-readable failure reason.
       */
      data class Failure(val reason: String) : RegisterUserResult
    }
  }
}
