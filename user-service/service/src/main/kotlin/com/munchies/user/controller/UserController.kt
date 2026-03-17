package com.munchies.user.controller

import com.munchies.user.api.UserApi
import com.munchies.user.api.UserDTO
import com.munchies.user.domain.UserId
import com.munchies.user.dto.toDTO
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.serde.annotation.SerdeImport

@SerdeImport(UserDTO::class)
@Controller(port = "8080")
class UserController(private val service: UserService) : UserApi<String, UserDTO> {
  @Get("/users/{id}/")
  override fun getUser(@PathVariable id: String): UserDTO = service
    .getUser(UserId(id))
    .toDTO()
}
