package com.munchies.user.infrastructure.adapter.inbound.web.controller

import com.munchies.user.application.port.inbound.CreateNewUser
import com.munchies.user.application.port.inbound.CreateNewUser.Companion.CreateNewUserResult
import com.munchies.user.application.port.inbound.GetUserQuery
import com.munchies.user.application.port.inbound.GetUserQuery.Companion.GetUserResult
import com.munchies.user.domain.model.UserId
import com.munchies.user.infrastructure.adapter.dto.UserDTO
import com.munchies.user.infrastructure.adapter.dto.mapper.toDTO
import com.munchies.user.infrastructure.adapter.inbound.UserAPI.Companion.AddUser
import com.munchies.user.infrastructure.adapter.inbound.UserAPI.Companion.GetUser
import com.munchies.user.infrastructure.adapter.inbound.web.config.UserServiceConfig
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.serde.annotation.SerdeImport
import jakarta.inject.Inject

@SerdeImport(UserDTO::class)
@Controller(
  port = UserServiceConfig.SERVICE_PORT.toString(),
  value = UserServiceConfig.SERVICE_PATH,
)
class MicronautUserController(
  @Inject
  private val getUser: GetUserQuery,
  @Inject
  private val createUser: CreateNewUser,
) : GetUser<String, HttpResponse<UserDTO>>, AddUser<HttpResponse<String>> {
  @Get("{id}/")
  override fun getUser(@PathVariable id: String): HttpResponse<UserDTO> {
    return when (val res = getUser.execute(UserId(id))) {
      is GetUserResult.Success -> HttpResponse.ok(res.user.toDTO())
      GetUserResult.NotFound -> HttpResponse.notFound()
    }
  }

  @Post("/")
  override fun addUser(): HttpResponse<String> {
    return when (val res = createUser.execute()) {
      is CreateNewUserResult.Success -> HttpResponse.created(res.userId.value)
    }
  }
}
