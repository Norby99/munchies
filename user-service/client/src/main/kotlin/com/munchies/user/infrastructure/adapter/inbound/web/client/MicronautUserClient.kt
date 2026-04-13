package com.munchies.user.infrastructure.adapter.inbound.web.client

import com.munchies.user.infrastructure.adapter.dto.UserDTO
import com.munchies.user.infrastructure.adapter.inbound.UserAPI.Companion.GetUserAPI
import com.munchies.user.infrastructure.adapter.inbound.UserAPI.Companion.LoginUserAPI
import com.munchies.user.infrastructure.adapter.inbound.UserAPI.Companion.RegisterUserAPI
import com.munchies.user.infrastructure.adapter.inbound.UserAPI.Companion.UpdateUserPasswordAPI
import com.munchies.user.infrastructure.adapter.inbound.request.RegisterUserRequest
import com.munchies.user.infrastructure.adapter.inbound.request.UpdateUserPasswordRequest
import com.munchies.user.infrastructure.adapter.inbound.web.config.UserServiceConfig
import io.micronaut.core.async.annotation.SingleResult
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Patch
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client

/**
 * Root contract for HTTP clients that communicate with the User service.
 *
 * The `@Client` annotation configures Micronaut to generate client implementations
 * using the base service path defined in [UserServiceConfig.SERVICE_PATH].
 *
 * Nested interfaces expose feature-specific API contracts so consumers can inject
 * only what they need (read, register, login, update password, update info).
 */
@Client(path = UserServiceConfig.SERVICE_PATH)
sealed interface MicronautUserClient {

  companion object {
    /**
     * Client contract for reading user data.
     */
    interface MicronautGetUser : GetUserAPI<String, HttpResponse<UserDTO>>, MicronautUserClient {
      /**
       * Fetches a user by identifier.
       *
       * @param id unique user identifier.
       * @return HTTP response containing the user payload when found.
       */
      @Get("{id}/")
      @SingleResult
      override fun getUser(id: String): HttpResponse<UserDTO>
    }

    /**
     * Client contract for user registration.
     */
    interface MicronautRegisterUser :
      RegisterUserAPI<RegisterUserRequest, HttpResponse<String>>, MicronautUserClient {
      /**
       * Sends a registration request to the user service.
       *
       * @param request registration payload as defined by the shared API contract.
       * @return HTTP response describing the registration outcome.
       */
      @Post("/register")
      @SingleResult
      override fun registerUser(request: RegisterUserRequest): HttpResponse<String>
    }

    /**
     * Client contract for user authentication.
     */
    interface MicronautLoginUser :
      LoginUserAPI<RegisterUserRequest, HttpResponse<String>>, MicronautUserClient {
      /**
       * Sends a login request to the user service.
       *
       * @param request authentication payload as defined by the shared API contract.
       * @return HTTP response describing the authentication outcome.
       */
      @Post("/login")
      @SingleResult
      override fun loginUser(request: RegisterUserRequest): HttpResponse<String>
    }

    /**
     * Client contract for password update operations.
     */
    interface MicronautUpdateUserPassword :
      UpdateUserPasswordAPI<UpdateUserPasswordRequest, HttpResponse<String>>, MicronautUserClient {
      /**
       * Requests a password update for a user.
       *
       * @param request payload containing user identification and password data.
       * @return HTTP response describing the password update outcome.
       */
      @Post("/update-password")
      @SingleResult
      override fun updateUserPassword(request: UpdateUserPasswordRequest): HttpResponse<String>
    }

    /**
     * Client contract for user info update operations.
     *
     * Note: this interface follows the currently declared shared API type and method
     * signature in the project contract.
     */
    interface MicronautUpdateUserInfo :
      UpdateUserPasswordAPI<UpdateUserPasswordRequest, HttpResponse<String>>,
      MicronautUserClient {
      /**
       * Requests an update of user information using the update-info endpoint.
       *
       * @param request payload as declared by the shared API contract.
       * @return HTTP response describing the update outcome.
       */
      @Patch("/update-info")
      @SingleResult
      override fun updateUserPassword(request: UpdateUserPasswordRequest): HttpResponse<String>
    }
  }
}
