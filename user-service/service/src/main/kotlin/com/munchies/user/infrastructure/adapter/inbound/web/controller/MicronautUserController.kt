package com.munchies.user.infrastructure.adapter.inbound.web.controller

import com.munchies.user.application.port.inbound.GetUser
import com.munchies.user.application.port.inbound.GetUser.Companion.GetUserResult
import com.munchies.user.application.port.inbound.LoginUser
import com.munchies.user.application.port.inbound.LoginUser.Companion.LoginResult
import com.munchies.user.application.port.inbound.RegisterUser
import com.munchies.user.domain.model.UserCredentials
import com.munchies.user.domain.model.UserId
import com.munchies.user.infrastructure.adapter.dto.UserDTO
import com.munchies.user.infrastructure.adapter.dto.factory.UserDTOFactory
import com.munchies.user.infrastructure.adapter.inbound.UserAPI.Companion.GetUserAPI
import com.munchies.user.infrastructure.adapter.inbound.UserAPI.Companion.LoginUserAPI
import com.munchies.user.infrastructure.adapter.inbound.UserAPI.Companion.RegisterUserAPI
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
 * - [GetUser] for fetching a user by id
 * - [RegisterUser] for registering a new user
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
  private val getUser: GetUser,

  /**
   * Use case for registering a new user in the application layer.
   */
  @Inject
  private val registerUser: RegisterUser,

  /**
   * Use case for authenticating a user in the application layer.
   */
  @Inject
  private val loginUser: LoginUser,

  /**
   * Factory for converting between User domain models and UserDTOs.
   */
  private val dtoFactory: UserDTOFactory = UserDTOFactory.default,
) :
  GetUserAPI<String, HttpResponse<UserDTO>>,
  RegisterUserAPI<UserDTO, HttpResponse<String>>,
  LoginUserAPI<UserDTO, HttpResponse<String>> {

  /**
   * Handles `GET /users/{id}/`.
   *
   * Translates the application-layer result into an HTTP response:
   * - `200 OK` with the user DTO if the user is found
   * - `404 Not Found` if the user does not exist
   *
   * @param id The user identifier received from the path.
   * @return An HTTP response containing the user DTO or a not-found status.
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
      is GetUserResult.Success -> HttpResponse.ok(dtoFactory.run { res.user.fromDomain() })
      GetUserResult.NotFound -> HttpResponse.notFound()
    }
  }

  /**
   * Handles `POST /register/`.
   *
   * Registers a new user with the provided information and credentials.
   * Translates the application-layer result into an HTTP response:
   * - `200 OK` if the user is successfully registered
   * - `400 Bad Request` if the user is already registered
   * - `500 Internal Server Error` if the registration fails
   *
   * @param userInfo The user information received in the request body.
   * @param hashedPassword The hashed password for the user.
   * @param saltValue The cryptographic salt used for hashing the password.
   * @return An HTTP response indicating the result of the registration process.
   */
  @Post("/register/")
  @Operation(
    summary = "Register a new user",
    description = "Registers a new user with the provided information and credentials.",
  )
  @ApiResponse(responseCode = "200", description = "User registered successfully")
  @ApiResponse(responseCode = "400", description = "User is already registered")
  @ApiResponse(responseCode = "500", description = "Failed to register user")
  override fun registerUser(
    userInfo: UserDTO,
    hashedPassword: String,
    saltValue: String,
  ): HttpResponse<String> {
    val user = dtoFactory.run { userInfo.fromDTO() }
    val userCredentials =
      UserCredentials(id = user.id, passwordHash = hashedPassword, salt = saltValue)

    return when (registerUser.execute(user, userCredentials)) {
      is RegisterUser.Companion.RegisterUserResult.Success ->
        HttpResponse.ok("User registered successfully")
      RegisterUser.Companion.RegisterUserResult.UserIsAlreadyRegistered ->
        HttpResponse.badRequest("User is already registered")
      is RegisterUser.Companion.RegisterUserResult.Failure ->
        HttpResponse
          .serverError("Failed to register user: ${registerUser.execute(user, userCredentials)}")
    }
  }

  @Get("/login/")
  @Operation(
    summary = "Login a user",
    description = "Authenticates a user with the provided email and password.",
  )
  @ApiResponse(responseCode = "200", description = "User logged in successfully")
  @ApiResponse(responseCode = "400", description = "Invalid email or password")
  override fun loginUser(user: UserDTO, providedPassword: String): HttpResponse<String> {
    return when (loginUser.execute(user.email, user.username, providedPassword)) {
      is LoginResult.Success -> HttpResponse.ok("Login successful")
      else -> HttpResponse.badRequest("Invalid email or password")
    }
  }
}
