package com.munchies.user.presentation

import com.munchies.user.infrastructure.config.UserServiceConfig
import com.munchies.user.presentation.dto.UserDTO
import io.micronaut.core.async.annotation.SingleResult
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.client.annotation.Client

@Client(path = UserServiceConfig.SERVICE_PATH)
interface UserClient : UserApi<String, HttpResponse<UserDTO>> {
  @Get("{id}/")
  @SingleResult
  override fun getUser(id: String): HttpResponse<UserDTO>

  fun handleUserNotFound(): HttpResponse<Void>
  companion object {
    class UserNotFoundException(id: String) : Exception("User $id not found")
  }
}
