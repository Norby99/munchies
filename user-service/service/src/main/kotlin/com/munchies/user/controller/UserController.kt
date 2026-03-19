package com.munchies.user.controller

import com.munchies.user.domain.model.UserId
import com.munchies.user.dto.toDTO
import com.munchies.user.infrastructure.api.UserApi
import com.munchies.user.infrastructure.api.dto.UserDTO
import com.munchies.user.infrastructure.config.UserServiceConfig
import com.munchies.user.infrastructure.service.Service
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.serde.annotation.SerdeImport
import jakarta.inject.Inject

@SerdeImport(UserDTO::class)
@Controller(port = "${UserServiceConfig.SERVICE_PORT}")
class UserController(
  @Inject
  private val service: Service.ControllerService,
) : UserApi<String, UserDTO> {
  @Get("/users/{id}/")
  override fun getUser(@PathVariable id: String): UserDTO = service
    .getUser(UserId(id))?.toDTO() ?: UserDTO(id)
}
