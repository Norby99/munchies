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
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.inject.Inject

/**
 * Micronaut HTTP controller for the user-service inbound web adapter.
 *
 * This controller exposes the user-related HTTP endpoints and delegates the
 * actual business logic to inbound application ports:
 * - [GetUserQuery] for fetching a user by id
 * - [CreateNewUser] for creating a new user
 *
 * The controller is kept thin on purpose so that the domain and application
 * layers remain independent from HTTP-specific concerns.
 */
@SerdeImport(UserDTO::class)
@Controller(
  port = UserServiceConfig.SERVICE_PORT.toString(),
  value = UserServiceConfig.SERVICE_PATH,
)
class MicronautUserController(
  /**
   * Use case for retrieving a user from the application layer.
   */
  @Inject
  private val getUser: GetUserQuery,

  /**
   * Use case for creating a new user from the application layer.
   */
  @Inject
  private val createUser: CreateNewUser,
) : GetUser<String, HttpResponse<UserDTO>>, AddUser<HttpResponse<String>> {

  /**
   * Handles `GET /users/{id}/`.
   *
   * Translates the application-layer result into an HTTP response:
   * - `200 OK` with the user DTO if the user is found
   * - `404 Not Found` if the user does not exist
   *
   * @param id the user identifier received from the path
   * @return an HTTP response containing the user DTO or a not-found status
   */
  @Get("{id}/")
  @Operation(
    summary = "Get user by id",
    description = "Retrieves a user by their unique identifier.",
  )
  @ApiResponse(responseCode = "200", description = "Found")
  @ApiResponse(responseCode = "404", description = "Not Found")
  override fun getUser(@PathVariable id: String): HttpResponse<UserDTO> {
    return when (val res = getUser.execute(UserId(id))) {
      is GetUserResult.Success -> HttpResponse.ok(res.user.toDTO())
      GetUserResult.NotFound -> HttpResponse.notFound()
    }
  }

  /**
   * Handles `POST /users/`.
   *
   * Delegates user creation to the application layer and returns:
   * - `201 Created` with the newly created user id on success
   *
   * @return an HTTP response containing the created user identifier
   */
  @Post("/")
  @Operation(
    summary = "Create a new user",
    description = "Creates a new user and returns their unique identifier.",
  )
  @ApiResponse(responseCode = "201", description = "Created")
  override fun addUser(): HttpResponse<String> {
    return when (val res = createUser.execute()) {
      is CreateNewUserResult.Success -> HttpResponse.created(res.userId.value)
    }
  }
}
