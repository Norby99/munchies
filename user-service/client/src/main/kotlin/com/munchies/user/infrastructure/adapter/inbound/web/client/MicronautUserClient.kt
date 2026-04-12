package com.munchies.user.infrastructure.adapter.inbound.web.client

import com.munchies.user.infrastructure.adapter.dto.RegisterUserRequest
import com.munchies.user.infrastructure.adapter.dto.UserDTO
import com.munchies.user.infrastructure.adapter.inbound.UserAPI.Companion.GetUserAPI
import com.munchies.user.infrastructure.adapter.inbound.UserAPI.Companion.LoginUserAPI
import com.munchies.user.infrastructure.adapter.inbound.UserAPI.Companion.RegisterUserAPI
import com.munchies.user.infrastructure.adapter.inbound.UserAPI.Companion.UpdateUserPasswordAPI
import com.munchies.user.infrastructure.adapter.inbound.web.config.UserServiceConfig
import io.micronaut.core.async.annotation.SingleResult
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client

/**
 * Micronaut HTTP client interface for interacting with the User service.
 */
@Client(path = UserServiceConfig.SERVICE_PATH)
sealed interface MicronautUserClient {

  companion object {
    /**
     * Client interface for retrieving user details.
     */
    interface MicronautGetUser : GetUserAPI<String, HttpResponse<UserDTO>>, MicronautUserClient {
      /**
       * Retrieves a user by their unique identifier.
       *
       * @param id The unique identifier of the user to retrieve.
       * @return An [HttpResponse] containing the [UserDTO] if found.
       */
      @Get("{id}/")
      @SingleResult
      override fun getUser(id: String): HttpResponse<UserDTO>
    }

    interface MicronautRegisterUser :
      RegisterUserAPI<RegisterUserRequest, HttpResponse<String>>, MicronautUserClient {
      /**
       * Registers a new user with the provided information.
       *
       * @param request The DTO containing the post info.
       * @return An [HttpResponse] containing the unique identifier of the newly registered user.
       */
      @Post("/register")
      @SingleResult
      override fun registerUser(request: RegisterUserRequest): HttpResponse<String>
    }

    interface MicronautLoginUser :
      LoginUserAPI<RegisterUserRequest, HttpResponse<String>>, MicronautUserClient {
      /**
       * Authenticates a user based on the provided credentials.
       *
       * @param request The DTO containing the post info.
       * @return An [HttpResponse] indicating the result of the login attempt.
       */
      @Post("/login")
      @SingleResult
      override fun loginUser(request: RegisterUserRequest): HttpResponse<String>
    }

    interface MicronautUpdateUserPassword :
      UpdateUserPasswordAPI<String, HttpResponse<String>>, MicronautUserClient {
      /**
       * Updates the password for a user.
       *
       * @param user The unique identifier of the user whose password is to be updated.
       * @param oldHashedPassword The new hashed password for the user.
       * @param newPassword The new salt value used for hashing the password.
       * @return An [HttpResponse] indicating the result of the password update attempt.
       */
      @Post("/update-password")
      @SingleResult
      override fun updateUserPassword(
        user: String,
        oldHashedPassword: String,
        newPassword: String,
      ): HttpResponse<String>
    }
  }
}
