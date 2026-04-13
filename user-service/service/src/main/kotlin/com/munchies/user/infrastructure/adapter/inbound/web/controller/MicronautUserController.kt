package com.munchies.user.infrastructure.adapter.inbound.web.controller

import com.munchies.user.application.port.inbound.GetUser
import com.munchies.user.application.port.inbound.GetUser.Companion.GetUserResult
import com.munchies.user.application.port.inbound.LoginUser
import com.munchies.user.application.port.inbound.LoginUser.Companion.LoginResult
import com.munchies.user.application.port.inbound.RegisterUser
import com.munchies.user.application.port.inbound.UpdateUserInfo
import com.munchies.user.application.port.inbound.UpdateUserPassword
import com.munchies.user.domain.model.UserCredentials
import com.munchies.user.domain.model.UserId
import com.munchies.user.infrastructure.adapter.dto.UserDTO
import com.munchies.user.infrastructure.adapter.dto.factory.UserDTOFactory
import com.munchies.user.infrastructure.adapter.inbound.UserAPI.Companion.GetUserAPI
import com.munchies.user.infrastructure.adapter.inbound.UserAPI.Companion.LoginUserAPI
import com.munchies.user.infrastructure.adapter.inbound.UserAPI.Companion.RegisterUserAPI
import com.munchies.user.infrastructure.adapter.inbound.UserAPI.Companion.UpdateUserInfoAPI
import com.munchies.user.infrastructure.adapter.inbound.UserAPI.Companion.UpdateUserPasswordAPI
import com.munchies.user.infrastructure.adapter.inbound.request.LoginUserRequest
import com.munchies.user.infrastructure.adapter.inbound.request.RegisterUserRequest
import com.munchies.user.infrastructure.adapter.inbound.request.UpdateUserInfoRequest
import com.munchies.user.infrastructure.adapter.inbound.request.UpdateUserPasswordRequest
import com.munchies.user.infrastructure.adapter.inbound.web.config.UserServiceConfig
import com.munchies.user.infrastructure.adapter.inbound.web.config.UserServices
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Patch
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
@SerdeImport(RegisterUserRequest::class)
@SerdeImport(LoginUserRequest::class)
@Controller(
  port = UserServiceConfig.SERVICE_PORT.toString(),
  value = UserServiceConfig.SERVICE_PATH,
)
class MicronautUserController(
  /**
   * Use case for retrieving a user from the application layer.
   */
  @Inject
  private val services: UserServices,

  /**
   * Factory for converting between User domain models and UserDTOs.
   */
  private val dtoFactory: UserDTOFactory = UserDTOFactory.default,
) :
  GetUserAPI<String, HttpResponse<UserDTO>>,
  RegisterUserAPI<RegisterUserRequest, HttpResponse<String>>,
  LoginUserAPI<LoginUserRequest, HttpResponse<String>>,
  UpdateUserPasswordAPI<UpdateUserPasswordRequest, HttpResponse<String>>,
  UpdateUserInfoAPI<UpdateUserInfoRequest, HttpResponse<String>> {
  private val getUser: GetUser = services.getUser
  private val registerUser: RegisterUser = services.registerUser
  private val loginUser: LoginUser = services.loginUser
  private val updateUserPassword: UpdateUserPassword = services.updateUserPassword
  private val updateUserInfo: UpdateUserInfo = services.updateUserInfo

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
   * @param request The DTO containing the post info.
   * @return An HTTP response indicating the result of the registration process.
   */
  @Post("/register/")
  @Operation(
    summary = "Register a new user",
    description = "Registers a new user with the provided information and credentials.",
  )
  @ApiResponse(responseCode = "200", description = "User registered successfully")
  @ApiResponse(responseCode = "400", description = "Invalid user data or missing fields")
  @ApiResponse(responseCode = "401", description = "User is already registered")
  @ApiResponse(responseCode = "500", description = "Failed to register user")
  override fun registerUser(@Body request: RegisterUserRequest): HttpResponse<String> {
    try {
      RegisterUserRequest.validate(request)
    } catch (e: IllegalArgumentException) {
      return HttpResponse.badRequest(e.message ?: "Invalid user data")
    }

    val user = dtoFactory.run { request.user.fromDTO() }
    val userCredentials =
      UserCredentials(id = user.id, passwordHash = request.hashedPassword, salt = request.saltValue)

    return when (
      registerUser.execute(
        user = user,
        credentials = userCredentials,
      )
    ) {
      is RegisterUser.Companion.RegisterUserResult.Success ->
        HttpResponse.ok("User registered successfully")
      RegisterUser.Companion.RegisterUserResult.UserIsAlreadyRegistered ->
        HttpResponse.unauthorized()
      is RegisterUser.Companion.RegisterUserResult.Failure ->
        HttpResponse
          .serverError("Failed to register user: ${registerUser.execute(user, userCredentials)}")
    }
  }

  @Post("/login/")
  @Operation(
    summary = "Login a user",
    description = "Authenticates a user with the provided email and password.",
  )
  @ApiResponse(responseCode = "200", description = "User logged in successfully")
  @ApiResponse(responseCode = "400", description = "Invalid email or password")
  @ApiResponse(
    responseCode = "401",
    description = "User is locked out due to too many failed login attempts",
  )
  override fun loginUser(@Body request: LoginUserRequest): HttpResponse<String> {
    try {
      LoginUserRequest.validate(request)
    } catch (e: IllegalArgumentException) {
      return HttpResponse.badRequest(e.message ?: "Invalid login data")
    }

    return when (
      loginUser.execute(
        email = request.email,
        username = request.username,
        password = request.password,
      )
    ) {
      is LoginResult.Success -> HttpResponse.ok("Login successful")
      is LoginResult.BlockedLogin -> HttpResponse.unauthorized()
      else -> HttpResponse.badRequest("Invalid email or password")
    }
  }

  @Post("/update-password/")
  @Operation(
    summary = "Update user password",
    description = "Updates the password for a user with the provided old and new passwords.",
  )
  @ApiResponse(responseCode = "200", description = "Password updated successfully")
  @ApiResponse(responseCode = "400", description = "Invalid request data or wrong old password")
  @ApiResponse(responseCode = "401", description = "User is locked out")
  @ApiResponse(responseCode = "404", description = "User not found")
  override fun updateUserPassword(@Body request: UpdateUserPasswordRequest): HttpResponse<String> {
    try {
      UpdateUserPasswordRequest.validate(request)
    } catch (e: IllegalArgumentException) {
      return HttpResponse.badRequest(e.message ?: "Invalid request data")
    }

    return when (
      updateUserPassword.execute(
        user = dtoFactory.run { request.user.fromDTO() },
        oldPassword = request.oldHashedPassword,
        newPassword = request.newPassword,
      )
    ) {
      is UpdateUserPassword.Companion.UpdateUserPasswordResult.Success ->
        HttpResponse.ok("Password updated successfully")
      UpdateUserPassword.Companion.UpdateUserPasswordResult.WrongCredentials ->
        HttpResponse.badRequest("Invalid old password")
      is UpdateUserPassword.Companion.UpdateUserPasswordResult.LockedUser ->
        HttpResponse.unauthorized()
      is UpdateUserPassword.Companion.UpdateUserPasswordResult.UserNotFound ->
        HttpResponse.notFound()
    }
  }

  @Patch("/update-info/")
  @Operation(
    summary = "Update user info",
    description = "Updates the profile information for a user.",
  )
  @ApiResponse(responseCode = "200", description = "User info updated successfully")
  @ApiResponse(responseCode = "400", description = "Invalid request data")
  @ApiResponse(responseCode = "404", description = "User not found")
  override fun updateUserInfo(request: UpdateUserInfoRequest): HttpResponse<String> {
    try {
      UpdateUserInfoRequest.validate(request)
    } catch (e: IllegalArgumentException) {
      return HttpResponse.badRequest(e.message ?: "Invalid request data")
    }

    return when (
      updateUserInfo.execute(
        user = dtoFactory.run { request.user.fromDTO() },
      )
    ) {
      is UpdateUserInfo.Companion.UpdateUserInfoResult.Success ->
        HttpResponse.ok("User info updated successfully")
      UpdateUserInfo.Companion.UpdateUserInfoResult.UserNotFound ->
        HttpResponse.notFound()
    }
  }
}
