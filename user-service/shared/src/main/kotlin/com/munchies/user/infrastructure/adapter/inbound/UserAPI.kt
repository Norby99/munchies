package com.munchies.user.infrastructure.adapter.inbound

/**
 * Contract namespace for user-related inbound API operations.
 *
 * This sealed interface groups operation-specific contracts used by adapters
 * (for example, HTTP controllers/clients) while keeping each use case focused.
 */
sealed interface UserAPI {
  companion object {
    /**
     * Contract for retrieving a user by identifier.
     *
     * @param UserId Type used to identify a user.
     * @param User Type returned by the operation.
     */
    interface GetUserAPI<UserId, User> : UserAPI {
      /**
       * Retrieves a user by its identifier.
       *
       * @param id user identifier.
       * @return the resolved user representation.
       */
      fun getUser(id: UserId): User
    }

    /**
     * Contract for registering a new user.
     *
     * @param Request request payload type.
     * @param Response response payload type.
     */
    interface RegisterUserAPI<Request, Response> : UserAPI {
      /**
       * Registers a user using the provided request payload.
       *
       * @param request registration input data.
       * @return operation response.
       */
      fun registerUser(request: Request): Response
    }

    /**
     * Contract for authenticating a user.
     *
     * @param Request request payload type.
     * @param Response response payload type.
     */
    interface LoginUserAPI<Request, Response> : UserAPI {
      /**
       * Authenticates a user with the provided credentials payload.
       *
       * @param request authentication input data.
       * @return operation response.
       */
      fun loginUser(request: Request): Response
    }

    /**
     * Contract for updating an existing user's password.
     *
     * @param Request request payload type.
     * @param Response response payload type.
     */
    interface UpdateUserPasswordAPI<Request, Response> : UserAPI {
      /**
       * Updates user password information.
       *
       * @param request password update input data.
       * @return operation response.
       */
      fun updateUserPassword(request: Request): Response
    }

    /**
     * Contract for updating an existing user's profile/info.
     *
     * @param Request request payload type.
     * @param Response response payload type.
     */
    interface UpdateUserInfoAPI<Request, Response> : UserAPI {
      /**
       * Updates user profile or account information.
       *
       * @param request user info update input data.
       * @return operation response.
       */
      fun updateUserInfo(request: Request): Response
    }
  }
}
