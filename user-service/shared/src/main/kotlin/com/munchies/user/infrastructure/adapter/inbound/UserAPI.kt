package com.munchies.user.infrastructure.adapter.inbound

/**
 * This interface defines the API for user-related operations.
 * It is a sealed interface that contains two sub-interfaces:
 * - GetUser: Defines a method to retrieve a user by their ID.
 * - AddUser: Defines a method to add a new user and return their ID.
 */
sealed interface UserAPI {
  companion object {
    /**
     * The GetUser interface defines a method to retrieve a user by their ID.
     * @param UserId The type of the user ID.
     * @param User The type of the user object.
     */
    interface GetUserAPI<UserId, User> : UserAPI {
      fun getUser(id: UserId): User
    }

    interface RegisterUserAPI<Request, Response> : UserAPI {
      fun registerUser(request: Request): Response
    }

    interface LoginUserAPI<Request, Response> : UserAPI {
      fun loginUser(request: Request): Response
    }

    interface UpdateUserPasswordAPI<Request, Response> : UserAPI {
      fun updateUserPassword(request: Request): Response
    }
  }
}
