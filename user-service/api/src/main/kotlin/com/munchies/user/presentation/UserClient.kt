package com.munchies.user.presentation

import com.munchies.user.presentation.dto.UserDTO
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.annotation.Client

@Client // TODO test
interface UserClient : UserApi<String, HttpResponse<UserDTO>> {
  override fun getUser(id: String): HttpResponse<UserDTO>
  fun handleUserNotFound(): HttpResponse<Void>
  companion object {
    class UserNotFoundException(val id: String) : Exception("User $id not found")
  }
}
