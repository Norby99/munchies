package com.munchies.user.infrastructure.adapter.inbound.web.client

import com.munchies.user.infrastructure.adapter.dto.UserDTO
import com.munchies.user.infrastructure.adapter.inbound.UserAPI.Companion.AddUser
import com.munchies.user.infrastructure.adapter.inbound.UserAPI.Companion.GetUser
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
    interface MicronautGetUser : GetUser<String, HttpResponse<UserDTO>>, MicronautUserClient {
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

    /**
     * Client interface for adding a new user.
     */
    interface MicronautAddUser : AddUser<HttpResponse<String>>, MicronautUserClient {
      /**
       * Submits a request to add a new user.
       *
       * @return An [HttpResponse] containing the created user identifier or confirmation message.
       */
      @Post("/")
      override fun addUser(): HttpResponse<String>
    }
  }
}
