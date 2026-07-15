package com.munchies.user.infrastructure.adapter.inbound.web.controller

import com.munchies.commons.domain.port.InvalidInput
import com.munchies.payment.infrastructure.adapter.dto.PaymentDetails
import com.munchies.payment.infrastructure.adapter.outbound.response.ProcessPaymentResponse
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
import com.munchies.user.infrastructure.adapter.outbound.http.PaymentService
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
@SerdeImport(GetUserResult::class)
@SerdeImport(GetUserRequest::class)
@SerdeImport(GetUserResponse::class)
@SerdeImport(GetUserFailure::class)
@SerdeImport(GetUserSuccess::class)
@SerdeImport(RegisterUserRequest::class)
@SerdeImport(RegisterUserResponse::class)
@SerdeImport(RegisterUserResult::class)
@SerdeImport(RegisterUserFailure::class)
@SerdeImport(RegisterUserSuccess::class)
@SerdeImport(LoginUserRequest::class)
@SerdeImport(LoginUserResponse::class)
@SerdeImport(LoginUserResult::class)
@SerdeImport(LoginUserFailure::class)
@SerdeImport(LoginUserSuccess::class)
@SerdeImport(GetUserResponse::class)
@SerdeImport(GetUserSuccess::class)
@SerdeImport(GetUserFailure::class)
@SerdeImport(UpdateUserInfoResponse::class)
@SerdeImport(UpdateUserInfoResult::class)
@SerdeImport(UpdateUserInfoRequest::class)
@SerdeImport(UpdateUserInfoSuccess::class)
@SerdeImport(UpdateUserInfoFailure::class)
@SerdeImport(UpdateUserPasswordResponse::class)
@SerdeImport(UpdateUserPasswordResult::class)
@SerdeImport(UpdateUserPasswordRequest::class)
@SerdeImport(UpdateUserPasswordSuccess::class)
@SerdeImport(UpdateUserPasswordFailure::class)
@SerdeImport(VerifyEmailResponse::class)
@SerdeImport(VerifyEmailResult::class)
@SerdeImport(VerifyEmailRequest::class)
@SerdeImport(VerifyEmailSuccess::class)
@SerdeImport(VerifyEmailFailure::class)
@Controller(
  port = UserServiceConfig.SERVICE_PORT.toString(),
  value = UserServiceConfig.SERVICE_PATH,
)
class MicronautUserController(
  /**
   * Aggregated application services exposing user-related use cases.
   */
  @Inject
  private val services: UserServices,

  @Inject
  private val paymentClient: PaymentService,

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
   * Lightweight diagnostic endpoint that exercises the payment client.
   *
   * This method is not part of the user API contract. It appears to be a
   * temporary or debugging-oriented endpoint that invokes the payment service
   * with placeholder data and returns the raw payment response.
   *
   * @return an HTTP 200 response containing the payment service result.
   */
  @Get("/")
  fun get(): HttpResponse<ProcessPaymentResponse> {
    println("GET / called")
    val response = paymentClient.processPayment(
      com.munchies.payment.infrastructure.adapter.inbound.request.ProcessPaymentRequest(
        orderId = "",
        PaymentDetails(
          amount = 0,
          currency = com.munchies.payment.infrastructure.adapter.dto.Currency.USD,
          method = com.munchies.payment.infrastructure.adapter.dto.PaymentMethod.CARD,
        ),
      ),
    )

    println("Response: $response")
    return HttpResponse.ok(
      response,
    )
  }

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
        GetUserResponse(
          GetUserSuccess(res.user.toDTO()),
          HttpStatus.OK.code,
        ),
      )
      GetUser.Companion.GetUserResult.NotFound -> HttpResponse.notFound(
        GetUserResponse(
          GetUserFailure("Not Found"),
          HttpStatus.NOT_FOUND.code,
        ),
      )
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
  @ApiResponse(responseCode = "200", description = "User registered successfully")
  @ApiResponse(responseCode = "400", description = "Invalid user data or missing fields")
  @ApiResponse(responseCode = "401", description = "User is already registered")
  @ApiResponse(responseCode = "500", description = "Failed to register user")
  override fun registerUser(
    @Body request: RegisterUserRequest,
  ): HttpResponse<RegisterUserResponse> {
    return when (val msg = RegisterUserRequestValidator().validate(request)) {
      is InvalidInput ->
        HttpResponse.badRequest(
          RegisterUserResponse(
            RegisterUserFailure(msg.reason),
            HttpStatus.BAD_REQUEST.code,
          ),
        )
      else -> {
        when (val user = request.user.toDomain()) {
          is UserDTOFactory.UserDTOFactoryResult.Failure ->
            HttpResponse.badRequest(
              RegisterUserResponse(
                RegisterUserFailure(user.reason),
                HttpStatus.BAD_REQUEST.code,
              ),
            )
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
                  .ok(
                    RegisterUserResponse(
                      RegisterUserSuccess(
                        res.user.toDTO(),
                      ),
                      HttpStatus.OK.code,
                    ),
                  )
              }
              is RegisterUser.Companion.RegisterUserResult.UserIsAlreadyRegistered ->
                HttpResponse
                  .unauthorized<RegisterUserResponse>().body(
                    RegisterUserResponse(
                      RegisterUserFailure(
                        "User is already registered",
                      ),
                      HttpStatus.UNAUTHORIZED.code,
                    ),
                  )
              is RegisterUser.Companion.RegisterUserResult.Failure ->
                HttpResponse
                  .serverError(
                    RegisterUserResponse(
                      RegisterUserFailure(
                        "An error has occurred: " + res.reason,
                      ),
                      HttpStatus.INTERNAL_SERVER_ERROR.code,
                    ),
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
      is InvalidInput -> HttpResponse.badRequest(
        LoginUserResponse(
          LoginUserFailure(
            msg.reason,
          ),
          HttpStatus.BAD_REQUEST.code,
        ),
      )
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
                  LoginUserSuccess(
                    result.userId,
                    result.role.toAuthRole(),
                  ),
                  HttpStatus.OK.code,
                ),
              )
          }
          is LoginResult.BlockedLogin ->
            HttpResponse
              .unauthorized<LoginUserResponse>()
              .body(
                LoginUserResponse(
                  LoginUserFailure("Unauthorized"),
                  HttpStatus.UNAUTHORIZED.code,
                ),
              )
          else ->
            HttpResponse
              .badRequest(
                LoginUserResponse(
                  LoginUserFailure(
                    "Invalid email or password",
                  ),
                  HttpStatus.BAD_REQUEST.code,
                ),
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
  @Post(UserServiceConfig.UPDATE_USER_PASSWORD_PATH)
  @Operation(
    summary = "Update user password",
    description = "Updates the password for a user with the provided old and new passwords.",
  )
  @ApiResponse(responseCode = "200", description = "Password updated successfully")
  @ApiResponse(responseCode = "400", description = "Invalid request data or wrong old password")
  @ApiResponse(responseCode = "401", description = "User is locked out")
  @ApiResponse(responseCode = "404", description = "User not found")
  override fun updateUserPassword(
    @Body request: UpdateUserPasswordRequest,
  ): HttpResponse<UpdateUserPasswordResponse> {
    return when (val msg = UpdateUserPasswordRequestValidator().validate(request)) {
      is InvalidInput ->
        HttpResponse.badRequest(
          UpdateUserPasswordResponse(
            UpdateUserPasswordFailure(msg.reason),
            HttpStatus.BAD_REQUEST.code,
          ),
        )
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
                UpdateUserPasswordSuccess(
                  "Password updated successfully",
                ),
                HttpStatus.OK.code,
              ),
            )

          is UpdateUserPassword.Companion.UpdateUserPasswordResult.WrongCredentials ->
            HttpResponse.badRequest(
              UpdateUserPasswordResponse(
                UpdateUserPasswordFailure(
                  "Invalid old password",
                ),
                HttpStatus.BAD_REQUEST.code,
              ),
            )

          is UpdateUserPassword.Companion.UpdateUserPasswordResult.LockedUser ->
            HttpResponse
              .unauthorized<UpdateUserPasswordResponse>()
              .body(
                UpdateUserPasswordResponse(
                  UpdateUserPasswordFailure(
                    "Unauthorized",
                  ),
                  HttpStatus.UNAUTHORIZED.code,
                ),
              )
          is UpdateUserPassword.Companion.UpdateUserPasswordResult.UnauthorizedOperation ->
            HttpResponse
              .unauthorized<UpdateUserPasswordResponse>()
              .body(
                UpdateUserPasswordResponse(
                  UpdateUserPasswordFailure(
                    "Unauthorized",
                  ),
                  HttpStatus.UNAUTHORIZED.code,
                ),
              )
          is UpdateUserPassword.Companion.UpdateUserPasswordResult.UserNotFound ->
            HttpResponse.notFound<UpdateUserPasswordResponse>()
              .body(
                UpdateUserPasswordResponse(
                  UpdateUserPasswordFailure(
                    "Not found",
                  ),
                  HttpStatus.NOT_FOUND.code,
                ),
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
      is InvalidInput ->
        HttpResponse
          .badRequest(
            UpdateUserInfoResponse(
              UpdateUserInfoFailure(
                msg.reason,
              ),
              HttpStatus.BAD_REQUEST.code,
            ),
          )
      else -> {
        when (val user = request.user.toDomain()) {
          is UserDTOFactory.UserDTOFactoryResult.Failure ->
            HttpResponse.badRequest(
              UpdateUserInfoResponse(
                UpdateUserInfoFailure(
                  user.reason,
                ),
                HttpStatus.BAD_REQUEST.code,
              ),
            )
          is UserDTOFactory.UserDTOFactoryResult.Success -> {
            when (
              val res =
                updateUserInfo.execute(
                  user = user.user,
                )
            ) {
              is UpdateUserInfo.Companion.UpdateUserInfoResult.Success ->
                HttpResponse.ok(
                  UpdateUserInfoResponse(
                    UpdateUserInfoSuccess("User info updated successfully"),
                    HttpStatus.OK.code,
                  ),
                )

              is UpdateUserInfo.Companion.UpdateUserInfoResult.UserNotFound ->
                HttpResponse
                  .notFound<UpdateUserInfoResponse>()
                  .body(
                    UpdateUserInfoResponse(
                      UpdateUserInfoFailure(
                        "Not Found",
                      ),
                      HttpStatus.NOT_FOUND.code,
                    ),
                  )

              is UpdateUserInfo.Companion.UpdateUserInfoResult.Failure ->
                HttpResponse.badRequest(
                  UpdateUserInfoResponse(
                    UpdateUserInfoFailure(res.reason),
                    HttpStatus.BAD_REQUEST.code,
                  ),
                )
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
      is InvalidInput -> HttpResponse.badRequest(
        DeleteUserResponse(
          DeleteUserFailure(msg.reason),
          HttpStatus.BAD_REQUEST.code,
        ),
      )
      else -> {
        when (val res = deleteUser.execute(UserId(id))) {
          is DeleteUser.Companion.DeleteUserResult.Success -> HttpResponse.ok(
            DeleteUserResponse(
              DeleteUserSuccess(
                res.user.toDTO(),
              ),
              HttpStatus.OK.code,
            ),
          )
          DeleteUser.Companion.DeleteUserResult.NotFound ->
            HttpResponse
              .notFound<DeleteUserResponse>()
              .body(
                DeleteUserResponse(
                  DeleteUserFailure("Not Found"),
                  HttpStatus.NOT_FOUND.code,
                ),
              )
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
      is InvalidInput -> HttpResponse.badRequest(
        VerifyEmailResponse(
          VerifyEmailFailure(msg.reason),
          HttpStatus.BAD_REQUEST.code,
        ),
      )
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
                VerifyEmailSuccess("Confirmed email successfully"),
                HttpStatus.OK.code,
              ),
            )
          }
          else -> HttpResponse.notFound<VerifyEmailResponse>().body(
            VerifyEmailResponse(
              VerifyEmailFailure("Not found"),
              HttpStatus.NOT_FOUND.code,
            ),
          )
        }
      }
    }
  }
}
