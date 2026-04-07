package com.munchies.user.application.port.inbound

import com.munchies.user.domain.model.UserId

/**
 * Inbound application port responsible for creating a new user.
 *
 * This interface belongs to the Application layer and defines the use case contract
 * that adapters (e.g., web controllers, messaging consumers) can invoke.
 */
interface CreateNewUser {

  /**
   * Executes the use case to create a new user.
   *
   * @return the outcome of the operation as a [CreateNewUserResult].
   */
  fun execute(): CreateNewUserResult

  companion object {
    /**
     * Represents the possible outcomes of the create-new-user use case.
     */
    sealed interface CreateNewUserResult {
      /**
       * Successful creation result containing the generated [UserId].
       *
       * @property userId unique identifier of the newly created user.
       */
      data class Success(val userId: UserId) : CreateNewUserResult
    }
  }
}
