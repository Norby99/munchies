package com.munchies.user.application.port.inbound

import com.munchies.user.domain.model.User
import com.munchies.user.domain.model.UserId

/**
 * Inbound application port for querying user data by identifier.
 *
 * This interface defines the use-case contract exposed to inbound adapters
 * (for example, REST controllers) in the Application layer.
 */
interface GetUserQuery {

  /**
   * Retrieves a user by its domain identifier.
   *
   * @param id the unique [UserId] of the user to fetch.
   * @return a [GetUserResult] representing either a successful lookup or a not-found outcome.
   */
  fun execute(id: UserId): GetUserResult

  companion object {
    /**
     * Sealed result type for the get-user query use case.
     */
    sealed interface GetUserResult {
      /**
       * Successful query outcome containing the requested [User].
       *
       * @property user the user found for the provided identifier.
       */
      data class Success(val user: User) : GetUserResult

      /**
       * Query outcome indicating that no user exists for the provided identifier.
       */
      object NotFound : GetUserResult
    }
  }
}
