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

@Client(path = UserServiceConfig.SERVICE_PATH)
sealed interface MicronautUserClient {

  companion object {
    interface MicronautGetUser : GetUser<String, HttpResponse<UserDTO>>, MicronautUserClient {
      @Get("{id}/")
      @SingleResult
      override fun getUser(id: String): HttpResponse<UserDTO>
    }
    interface MicronautAddUser : AddUser<HttpResponse<String>>, MicronautUserClient {
      @Post("/")
      override fun addUser(): HttpResponse<String>
    }
  }
}
