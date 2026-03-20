package com.munchies.user.infrastructure.controller

import com.munchies.user.domain.model.UserId
import com.munchies.user.infrastructure.api.UserClient
import com.munchies.user.infrastructure.api.dto.UserDTO
import com.munchies.user.infrastructure.config.UserServiceConfig
import com.munchies.user.infrastructure.service.Service
import com.munchies.user.presentation.toDTO
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.serde.annotation.SerdeImport
import jakarta.inject.Inject

@SerdeImport(UserDTO::class)
@Controller(port = "${UserServiceConfig.SERVICE_PORT}")
class UserController(
  @Inject
  private val service: Service.ControllerService,
) : UserClient {
  @Get("/users/{id}/")
  override fun getUser(@PathVariable id: String): HttpResponse<UserDTO> {
    val user = service.getUser(UserId(id)) ?: throw UserClient.Companion.UserNotFoundException(id)
    return HttpResponse.ok(user.toDTO())
  }

  @Error(exception = UserClient.Companion.UserNotFoundException::class)
  override fun handleUserNotFound(): HttpResponse<Void> = HttpResponse.notFound()
}
