package com.munchies.user.adapter.inbound.web.controller

import com.munchies.user.adapter.config.UserServiceConfig
import com.munchies.user.application.port.inbound.CreateNewUser
import com.munchies.user.application.port.inbound.CreateNewUser.Companion.CreateNewUserResult
import com.munchies.user.application.port.inbound.GetUserQuery
import com.munchies.user.application.port.inbound.GetUserQuery.Companion.GetUserResult
import com.munchies.user.domain.model.UserId
import com.munchies.user.presentation.UserClient
import com.munchies.user.presentation.dto.UserDTO
import com.munchies.user.presentation.toDTO
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
class UserController(
  @Inject
  private val getUser: GetUserQuery,
  @Inject
  private val createUser: CreateNewUser,
) : UserClient {
  @Get("{id}/")
  override fun getUser(@PathVariable id: String): HttpResponse<UserDTO> {
    println("retrieving user with id: $id")
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
