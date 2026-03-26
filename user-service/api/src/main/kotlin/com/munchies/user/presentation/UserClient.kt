package com.munchies.user.presentation

import com.munchies.user.adapter.config.UserServiceConfig
import com.munchies.user.presentation.dto.UserDTO
import io.micronaut.core.async.annotation.SingleResult
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client

@Client(path = UserServiceConfig.SERVICE_PATH)
interface UserClient {
  @Get("{id}/")
  @SingleResult
  fun getUser(id: String): HttpResponse<UserDTO>

  @Post("/")
  fun addUser(): HttpResponse<String>
}
