package com.munchies.user.infrastructure.adapter.inbound.web.controller

import com.munchies.commons.domain.port.InvalidInput
import com.munchies.commons.domain.port.ValidationException
import com.munchies.commons.infrastructure.adapter.ErrorResponse
import com.munchies.user.application.port.inbound.*
import com.munchies.user.application.port.inbound.LoginUser.Companion.LoginResult
import com.munchies.user.domain.model.UserCredentials
import com.munchies.user.domain.model.UserId
import com.munchies.user.infrastructure.adapter.dto.UserDTO
import com.munchies.user.infrastructure.adapter.dto.factory.UserDTOFactory
import com.munchies.user.infrastructure.adapter.dto.factory.UserDTOFactory.toAuthRole
import com.munchies.user.infrastructure.adapter.dto.factory.UserDTOFactory.toDTO
import com.munchies.user.infrastructure.adapter.dto.factory.UserDTOFactory.toDomain
import com.munchies.user.infrastructure.adapter.inbound.UserAPI
import com.munchies.user.infrastructure.adapter.inbound.request.*
import com.munchies.user.infrastructure.adapter.inbound.web.config.UserServiceConfig
import com.munchies.user.infrastructure.adapter.inbound.web.config.UserServices
import com.munchies.user.infrastructure.adapter.inbound.web.controller.exception.FactoryException
import com.munchies.user.infrastructure.adapter.inbound.web.controller.exception.NotFoundException
import com.munchies.user.infrastructure.adapter.inbound.web.controller.exception.UnauthorizedException
import com.munchies.user.infrastructure.adapter.inbound.web.controller.exception.UnexpectedException
import com.munchies.user.infrastructure.adapter.outbound.kafka.EmailConfirmationClient
import com.munchies.user.infrastructure.adapter.outbound.notification.UserEmailConfirmationNotification
import com.munchies.user.infrastructure.adapter.outbound.notification.UserEmailConfirmationNotificationInfo.USER_CONFIRMATION_KEY
import com.munchies.user.infrastructure.adapter.outbound.response.*
import com.munchies.user.infrastructure.adapter.validator.*
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.*
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.micronaut.serde.annotation.SerdeImport
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.inject.Inject

/**
 * Micronaut HTTP controller for the user-service inbound web adapter.
 *
 * This controller exposes user-related HTTP endpoints and delegates business
 * operations to application inbound ports. It is responsible for:
 * - validating incoming request payloads at the HTTP boundary
 * - mapping DTOs to domain objects via [UserDTOFactory]
 * - translating use-case results into HTTP response codes and messages
 *
 * The controller intentionally avoids domain logic so the application and domain
 * layers remain independent from transport concerns.
 */
@ExecuteOn(TaskExecutors.BLOCKING)
@SerdeImport(UserDTO::class)
@SerdeImport(GetUserRequest::class)
@SerdeImport(GetUserResponse::class)
@SerdeImport(RegisterUserRequest::class)
@SerdeImport(RegisterUserResponse::class)
@SerdeImport(LoginUserRequest::class)
@SerdeImport(LoginUserResponse::class)
@SerdeImport(LoginUserResult::class)
@SerdeImport(UpdateUserInfoRequest::class)
@SerdeImport(UpdateUserInfoResponse::class)
@SerdeImport(UpdateUserPasswordRequest::class)
@SerdeImport(UpdateUserPasswordResponse::class)
@SerdeImport(VerifyEmailRequest::class)
@SerdeImport(VerifyEmailResponse::class)
@SerdeImport(DeleteUserRequest::class)
@SerdeImport(DeleteUserResponse::class)
@SerdeImport(ErrorResponse::class)
@SerdeImport(FactoryException::class)
@SerdeImport(NotFoundException::class)
@SerdeImport(UnauthorizedException::class)
@SerdeImport(UnexpectedException::class)
@SerdeImport(ValidationException::class)
@Controller(
  value = UserServiceConfig.SERVICE_PATH,
)
class MicronautUserController(
  /**
   * Aggregated application services exposing user-related use cases.
   */
  @Inject
  private val services: UserServices,

  /**
   * Kafka producer/client used to emit email confirmation events after a user
   * successfully verifies their email address.
   */
  @Inject
  private val emailConfirmationKafkaClient: EmailConfirmationClient,
) :
  UserAPI.GetUserAPI<HttpResponse<GetUserResponse>>,
  UserAPI.RegisterUserAPI<HttpResponse<RegisterUserResponse>>,
  UserAPI.LoginUserAPI<HttpResponse<LoginUserResponse>>,
  UserAPI.UpdateUserPasswordAPI<HttpResponse<UpdateUserPasswordResponse>>,
  UserAPI.UpdateUserInfoAPI<HttpResponse<UpdateUserInfoResponse>>,
  UserAPI.DeleteUserAPI<HttpResponse<DeleteUserResponse>>,
  UserAPI.EmailVerificationAPI<HttpResponse<VerifyEmailResponse>> {
  /**
   * Application use case for retrieving a user by identifier.
   */
  private val getUser: GetUser = services.getUser

  /**
   * Application use case for registering a new user.
   */
  private val registerUser: RegisterUser = services.registerUser

  /**
   * Application use case for authenticating a user.
   */
  private val loginUser: LoginUser = services.loginUser

  /**
   * Application use case for updating a user's password.
   */
  private val updateUserPassword: UpdateUserPassword = services.updateUserPassword

  /**
   * Application use case for updating user profile data.
   */
  private val updateUserInfo: UpdateUserInfo = services.updateUserInfo

  /**
   * Application use case for deleting a user.
   */
  private val deleteUser: DeleteUser = services.deleteUser

  /**
   * Application use case for verifying a user's email address.
   */
  private val verifyUserEmail: VerifyUserEmail = services.verifyUserEmail

  /**
   * Handles `GET users/{id}/`.
   *
   * Translates the application-layer result into an HTTP response:
   * - `200 OK` with the user DTO if the user is found
   * - `404 Not Found` if the user does not exist
   *
   * @param id The user identifier received from the path.
   * @return An HTTP response containing the user DTO or a not-found status.
   */
  @Get(UserServiceConfig.GET_USER_PATH)
  @Operation(
    summary = "Get user by id",
    description = "Retrieves a user by their unique identifier.",
  )
  @ApiResponse(responseCode = "200", description = "Found")
  @ApiResponse(responseCode = "404", description = "Not Found")
  override fun getUser(@PathVariable id: String): HttpResponse<GetUserResponse> {
    return when (val res = getUser.execute(UserId(id))) {
      is GetUser.Companion.GetUserResult.Success -> HttpResponse.ok(
        GetUserResponse(result = res.user.toDTO(), code = HttpStatus.OK.code),
      )
      GetUser.Companion.GetUserResult.NotFound -> throw NotFoundException("User not found")
    }
  }

  /**
   * Handles `POST users/register/`.
   *
   * Validates input data, maps incoming DTOs to domain entities, and delegates
   * registration to the application layer.
   *
   * Response mapping:
   * - `200 OK` when registration succeeds
   * - `400 Bad Request` when payload validation fails
   * - `401 Unauthorized` when the user is already registered
   * - `500 Internal Server Error` when the registration use case reports a failure
   *
   * @param request Registration payload containing user data and credentials data.
   * @return An HTTP response representing the registration outcome.
   */
  @Post(UserServiceConfig.REGISTER_USER_PATH)
  @Operation(
    summary = "Register a new user",
    description = "Registers a new user with the provided information and credentials.",
  )
  @ApiResponse(responseCode = "201", description = "User registered successfully")
  @ApiResponse(responseCode = "400", description = "Invalid user data or missing fields")
  @ApiResponse(responseCode = "401", description = "User is already registered")
  @ApiResponse(responseCode = "500", description = "Failed to register user")
  override fun registerUser(
    @Body request: RegisterUserRequest,
  ): HttpResponse<RegisterUserResponse> {
    return when (val msg = RegisterUserRequestValidator().validate(request)) {
      is InvalidInput ->
        throw ValidationException(msg.reason)
      else -> {
        when (val user = request.user.toDomain()) {
          is UserDTOFactory.UserDTOFactoryResult.Failure ->
            throw FactoryException(user.reason)
          is UserDTOFactory.UserDTOFactoryResult.Success -> {
            val userCredentials =
              UserCredentials(
                id = user.user.id,
                passwordHash = request.hashedPassword,
                salt = request.saltValue,
              )
            when (
              val res =
                registerUser.execute(
                  user = user.user,
                  credentials = userCredentials,
                )
            ) {
              is RegisterUser.Companion.RegisterUserResult.Success -> {
                HttpResponse
                  .created(
                    RegisterUserResponse(
                      result = res.user.toDTO(),
                      code = HttpStatus.CREATED.code,
                    ),
                  )
              }
              is RegisterUser.Companion.RegisterUserResult.UserIsAlreadyRegistered ->
                throw UnauthorizedException(
                  "User is already registered",
                )
              is RegisterUser.Companion.RegisterUserResult.Failure ->
                throw UnexpectedException(
                  "An error has occurred: " + res.reason,
                )
            }
          }
        }
      }
    }
  }

  /**
   * Handles `POST users/login/`.
   *
   * Validates request payload and delegates authentication to [LoginUser].
   * The endpoint supports either email or username as identifier.
   *
   * Response mapping:
   * - `200 OK` when authentication succeeds
   * - `400 Bad Request` when payload validation fails or credentials are invalid
   * - `401 Unauthorized` when login is blocked (for example, due to lockout rules)
   *
   * @param request Login payload containing identifier (`email` or `username`) and `password`.
   * @return An HTTP response indicating authentication success or failure.
   */
  @Post(UserServiceConfig.LOGIN_USER_PATH)
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
  override fun loginUser(@Body request: LoginUserRequest): HttpResponse<LoginUserResponse> {
    return when (val msg = LoginUserRequestValidator().validate(request)) {
      is InvalidInput -> throw ValidationException(msg.reason)
      else -> {
        when (
          val result =
            loginUser.execute(
              email = request.email,
              username = request.username,
              password = request.password,
            )
        ) {
          is LoginResult.Success -> {
            HttpResponse
              .ok(
                LoginUserResponse(
                  result.userId,
                  result.role.toAuthRole(),
                ),
              )
          }
          is LoginResult.BlockedLogin ->
            throw UnauthorizedException("User is locked")
          is LoginResult.NotFound ->
            throw NotFoundException("User was not found")
          else ->
            throw UnexpectedException(
              "Invalid email or password",
            )
        }
      }
    }
  }

  /**
   * Handles `POST users/update-password/`.
   *
   * Validates the payload and delegates password update workflow to
   * [UpdateUserPassword], including old-password verification and account lock checks.
   *
   * Response mapping:
   * - `200 OK` when password is updated
   * - `400 Bad Request` for invalid payload or wrong old password
   * - `401 Unauthorized` when user is currently locked
   * - `404 Not Found` when user or credentials cannot be found
   *
   * @param request Password update payload containing user, old password, and new password.
   * @return An HTTP response representing the update result.
   */
  @Patch(UserServiceConfig.UPDATE_USER_PASSWORD_PATH)
  @Operation(
    summary = "Update user password",
    description = "Updates the password for a user with the provided old and new passwords.",
  )
  @ApiResponse(responseCode = "200", description = "Password updated successfully")
  @ApiResponse(responseCode = "401", description = "Invalid request data or wrong old password")
  @ApiResponse(responseCode = "401", description = "User is locked out")
  @ApiResponse(responseCode = "404", description = "User not found")
  override fun updateUserPassword(
    @Body request: UpdateUserPasswordRequest,
  ): HttpResponse<UpdateUserPasswordResponse> {
    return when (val msg = UpdateUserPasswordRequestValidator().validate(request)) {
      is InvalidInput ->
        throw ValidationException(msg.reason)
      else -> {
        when (
          updateUserPassword.execute(
            id = request.id,
            username = request.username,
            email = request.email,
            oldPassword = request.oldHashedPassword,
            newPassword = request.newPassword,
          )
        ) {
          is UpdateUserPassword.Companion.UpdateUserPasswordResult.Success ->
            HttpResponse.ok(
              UpdateUserPasswordResponse(
                "Password updated successfully",
              ),
            )

          is UpdateUserPassword.Companion.UpdateUserPasswordResult.WrongCredentials ->
            throw UnauthorizedException(
              "Invalid old password",
            )

          is UpdateUserPassword.Companion.UpdateUserPasswordResult.LockedUser ->
            throw UnauthorizedException(
              "Locked user",
            )
          is UpdateUserPassword.Companion.UpdateUserPasswordResult.UnauthorizedOperation ->
            throw UnauthorizedException(
              "Unauthorized operation",
            )
          is UpdateUserPassword.Companion.UpdateUserPasswordResult.UserNotFound ->
            throw NotFoundException(
              "Not found",
            )
        }
      }
    }
  }

  /**
   * Handles `PATCH users/update-info/`.
   *
   * Validates request data and delegates user profile updates to [UpdateUserInfo].
   *
   * Response mapping:
   * - `200 OK` when user info is updated
   * - `400 Bad Request` when payload validation fails
   * - `404 Not Found` when the target user does not exist
   *
   * @param request Payload containing the updated user profile information.
   * @return An HTTP response representing the update result.
   */
  @Patch(UserServiceConfig.UPDATE_USER_INFO_PATH)
  @Operation(
    summary = "Update user info",
    description = "Updates the profile information for a user.",
  )
  @ApiResponse(responseCode = "200", description = "User info updated successfully")
  @ApiResponse(responseCode = "400", description = "Invalid request data")
  @ApiResponse(responseCode = "404", description = "User not found")
  override fun updateUserInfo(
    @Body request: UpdateUserInfoRequest,
  ): HttpResponse<UpdateUserInfoResponse> {
    return when (val msg = UpdateUserInfoRequestValidator().validate(request)) {
      is InvalidInput -> {
        throw ValidationException(
          msg.reason,
        )
      }
      else -> {
        when (val user = request.user.toDomain()) {
          is UserDTOFactory.UserDTOFactoryResult.Failure -> {
            throw FactoryException(
              user.reason,
            )
          }
          is UserDTOFactory.UserDTOFactoryResult.Success -> {
            when (
              val res =
                updateUserInfo.execute(
                  user = user.user,
                )
            ) {
              is UpdateUserInfo.Companion.UpdateUserInfoResult.Success ->
                HttpResponse.ok(
                  UpdateUserInfoResponse("User info updated successfully"),
                )

              is UpdateUserInfo.Companion.UpdateUserInfoResult.UserNotFound ->
                throw NotFoundException(
                  "Not Found",
                )
              is UpdateUserInfo.Companion.UpdateUserInfoResult.Failure -> {
                throw UnexpectedException(
                  res.reason,
                )
              }
            }
          }
        }
      }
    }
  }

  /**
   * Handles `DELETE users/{id}/`.
   *
   * Validates the incoming identifier and delegates deletion to the application layer.
   * The endpoint returns the deleted user payload when the operation succeeds.
   *
   * @param id The user identifier from the path.
   * @return An HTTP response indicating whether the user was deleted.
   */
  @Delete(UserServiceConfig.DELETE_USER_PATH)
  @Operation(
    summary = "Deletes a user.",
    description = "Deletes a user from an id",
  )
  @ApiResponse(responseCode = "200", description = "User deleted successfully")
  @ApiResponse(responseCode = "404", description = "User not found")
  override fun deleteUser(@PathVariable id: String): HttpResponse<DeleteUserResponse> {
    return when (val msg = DeleteUserRequestValidator().validate(DeleteUserRequest(id))) {
      is InvalidInput -> throw ValidationException(msg.reason)
      else -> {
        when (val res = deleteUser.execute(UserId(id))) {
          is DeleteUser.Companion.DeleteUserResult.Success -> HttpResponse.ok(
            DeleteUserResponse(
              res.user.toDTO(),
            ),
          )
          DeleteUser.Companion.DeleteUserResult.NotFound ->
            throw NotFoundException("Not Found")
        }
      }
    }
  }

  /**
   * Handles email verification using the one-time key received by the user.
   *
   * On successful verification, the controller emits a confirmation event to
   * Kafka so downstream services can react to the confirmed email state.
   *
   * @param request Verification payload containing the user identifier and OTP/key.
   * @return An HTTP response indicating confirmation success or failure.
   */
  @Get(UserServiceConfig.VERIFY_EMAIL_PATH)
  @Operation(
    summary = "Confirm a user's email address",
    description = "The user inputs a specific otk received in the email",
  )
  @ApiResponse(responseCode = "200", description = "Email confirmed successfully")
  @ApiResponse(responseCode = "404", description = "User not found or otk not valid")
  override fun verifyEmail(@Body request: VerifyEmailRequest): HttpResponse<VerifyEmailResponse> {
    val request = VerifyEmailRequest(request.id, request.otk)
    return when (val msg = VerifyEmailRequestValidator().validate(request)) {
      is InvalidInput -> throw ValidationException(msg.reason)
      else -> {
        when (verifyUserEmail.execute(request.id, request.otk)) {
          is VerifyUserEmail.Companion.VerifyUserEmailResult.ConfirmedEmail -> {
            emailConfirmationKafkaClient.confirmEmail(
              UserEmailConfirmationNotification(
                request.id,
                USER_CONFIRMATION_KEY,
              ).toJson(),
            )
            HttpResponse.ok(
              VerifyEmailResponse(
                "Confirmed email successfully",
              ),
            )
          }
          else -> throw NotFoundException("Not found")
        }
      }
    }
  }
}
