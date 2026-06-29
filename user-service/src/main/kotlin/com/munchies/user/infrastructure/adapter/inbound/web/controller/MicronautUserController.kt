package com.munchies.user.infrastructure.adapter.inbound.web.controller

import com.munchies.commons.domain.port.GenerateTokenFailure
import com.munchies.commons.domain.port.GenerateTokenSuccess
import com.munchies.commons.domain.port.InvalidInput
import com.munchies.commons.domain.port.TokenProvider
import com.munchies.payment.infrastructure.adapter.dto.PaymentDetails
import com.munchies.payment.infrastructure.adapter.outbound.response.ProcessPaymentResponse
import com.munchies.user.application.port.inbound.*
import com.munchies.user.application.port.inbound.LoginUser.Companion.LoginResult
import com.munchies.user.domain.model.UserCredentials
import com.munchies.user.domain.model.UserId
import com.munchies.user.infrastructure.adapter.dto.UserDTO
import com.munchies.user.infrastructure.adapter.dto.factory.UserDTOFactory
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
import io.micronaut.http.cookie.Cookie
import io.micronaut.http.cookie.SameSite
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
@SerdeImport(RegisterUserRequest::class)
@SerdeImport(LoginUserRequest::class)
@SerdeImport(GetUserResponse::class)
@SerdeImport(GetUserSuccess::class)
@SerdeImport(GetUserFailure::class)
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

  @Inject
  private val emailConfirmationKafkaClient: EmailConfirmationClient,

  @Inject val tokenProvider: TokenProvider,
) :
  UserAPI.GetUserAPI<HttpResponse<GetUserResponse>>,
  UserAPI.RegisterUserAPI<HttpResponse<RegisterUserResponse>>,
  UserAPI.LoginUserAPI<HttpResponse<LoginUserResponse>>,
  UserAPI.UpdateUserPasswordAPI<HttpResponse<UpdateUserPasswordResponse>>,
  UserAPI.UpdateUserInfoAPI<HttpResponse<UpdateUserInfoResponse>>,
  UserAPI.DeleteUserAPI<HttpResponse<DeleteUserResponse>>,
  UserAPI.EmailVerificationAPI<HttpResponse<UserDTO>> {
  private val getUser: GetUser = services.getUser
  private val registerUser: RegisterUser = services.registerUser
  private val loginUser: LoginUser = services.loginUser
  private val updateUserPassword: UpdateUserPassword = services.updateUserPassword
  private val updateUserInfo: UpdateUserInfo = services.updateUserInfo
  private val deleteUser: DeleteUser = services.deleteUser
  private val verifyUserEmail: VerifyUserEmail = services.verifyUserEmail

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
        GetUserResponse(GetUserSuccess(res.user.toDTO())),
      )
      GetUser.Companion.GetUserResult.NotFound -> HttpResponse.notFound(
        GetUserResponse(GetUserFailure("Not Found")),
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
        HttpResponse.badRequest(RegisterUserResponse(RegisterUserFailure(msg.reason)))
      else -> {
        when (val user = request.user.toDomain()) {
          is UserDTOFactory.UserDTOFactoryResult.Failure ->
            HttpResponse.badRequest(
              RegisterUserResponse(
                RegisterUserFailure(user.reason),
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
                when (val token = tokenProvider.generateToken(user.user.id)) {
                  is GenerateTokenSuccess -> {
                    HttpResponse
                      .ok(
                        RegisterUserResponse(
                          RegisterUserSuccess(
                            "User registered successfully",
                          ),
                        ),
                      )
                      .cookie(
                        Cookie.of("authToken", token.token)
                          .httpOnly(true)
                          .secure(true)
                          .sameSite(SameSite.Strict)
                          .path(UserServiceConfig.SERVICE_PATH),
                      )
                  }
                  else ->
                    HttpResponse.serverError(
                      RegisterUserResponse(
                        RegisterUserFailure(
                          "Couldn't generate token",
                        ),
                      ),
                    )
                }
              }
              is RegisterUser.Companion.RegisterUserResult.UserIsAlreadyRegistered ->
                HttpResponse
                  .unauthorized<RegisterUserResponse>().body(
                    RegisterUserResponse(
                      RegisterUserFailure(
                        "User is already registered",
                      ),
                    ),
                  )
              is RegisterUser.Companion.RegisterUserResult.Failure ->
                HttpResponse
                  .serverError(
                    RegisterUserResponse(
                      RegisterUserFailure(
                        "An error has occurred: " + res.reason,
                      ),
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
            when (val token = tokenProvider.generateToken(UserId(result.userId))) {
              is GenerateTokenSuccess ->
                HttpResponse
                  .ok(
                    LoginUserResponse(
                      LoginUserSuccess(
                        "User logged successfully",
                      ),
                    ),
                  )
                  .cookie(
                    Cookie.of("authToken", token.token)
                      .httpOnly(true)
                      .secure(true)
                      .sameSite(SameSite.Strict)
                      .path(UserServiceConfig.SERVICE_PATH),
                  )
              is GenerateTokenFailure ->
                HttpResponse
                  .serverError(
                    LoginUserResponse(
                      LoginUserFailure(
                        "Couldn't create token",
                      ),
                    ),
                  )
            }
          }
          is LoginResult.BlockedLogin ->
            HttpResponse
              .unauthorized<LoginUserResponse>()
              .body(
                LoginUserResponse(
                  LoginUserFailure("Unauthorized"),
                ),
              )
          else ->
            HttpResponse
              .badRequest(
                LoginUserResponse(
                  LoginUserFailure(
                    "Invalid email or password",
                  ),
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
              ),
            )

          UpdateUserPassword.Companion.UpdateUserPasswordResult.WrongCredentials ->
            HttpResponse.badRequest(
              UpdateUserPasswordResponse(
                UpdateUserPasswordFailure(
                  "Invalid old password",
                ),
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
                ),
              )
          is UpdateUserPassword.Companion.UpdateUserPasswordResult.UserNotFound ->
            HttpResponse.notFound<UpdateUserPasswordResponse>()
              .body(UpdateUserPasswordResponse(UpdateUserPasswordFailure("Not found")))
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
              UpdateUserInfoFailure(msg.reason),
            ),
          )
      else -> {
        when (val user = request.user.toDomain()) {
          is UserDTOFactory.UserDTOFactoryResult.Failure ->
            HttpResponse.badRequest(
              UpdateUserInfoResponse(UpdateUserInfoFailure(user.reason)),
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
                  ),
                )

              is UpdateUserInfo.Companion.UpdateUserInfoResult.UserNotFound ->
                HttpResponse
                  .notFound<UpdateUserInfoResponse>()
                  .body(UpdateUserInfoResponse(UpdateUserInfoFailure("Not Found")))

              is UpdateUserInfo.Companion.UpdateUserInfoResult.Failure ->
                HttpResponse.badRequest(
                  UpdateUserInfoResponse(UpdateUserInfoFailure(res.reason)),
                )
            }
          }
        }
      }
    }
  }

  @Delete(UserServiceConfig.DELETE_USER_PATH)
  @Operation(
    summary = "Deletes a user.",
    description = "Deletes a user from an id",
  )
  @ApiResponse(responseCode = "200", description = "User deleted successfully")
  @ApiResponse(responseCode = "404", description = "User not found")
  override fun deleteUser(@Body request: DeleteUserRequest): HttpResponse<DeleteUserResponse> {
    return when (val msg = DeleteUserRequestValidator().validate(request)) {
      is InvalidInput -> HttpResponse.badRequest(
        DeleteUserResponse(DeleteUserFailure(msg.reason)),
      )
      else -> {
        when (val res = deleteUser.execute(UserId(request.userId))) {
          is DeleteUser.Companion.DeleteUserResult.Success -> HttpResponse.ok(
            DeleteUserResponse(
              DeleteUserSuccess(
                res.user.toDTO(),
              ),
            ),
          )
          DeleteUser.Companion.DeleteUserResult.NotFound ->
            HttpResponse
              .notFound<DeleteUserResponse>()
              .body(DeleteUserResponse(DeleteUserFailure("Not Found")))
        }
      }
    }
  }

  @Get(UserServiceConfig.VERIFY_EMAIL_PATH)
  @Operation(
    summary = "Confirm a user's email address",
    description = "The user inputs a specific otk received in the email",
  )
  @ApiResponse(responseCode = "200", description = "Email confirmed successfully")
  @ApiResponse(responseCode = "404", description = "User not found or otk not valid")
  override fun verifyEmail(@QueryValue id: String, @QueryValue otk: String): HttpResponse<UserDTO> {
    val request = VerifyEmailRequest(id, otk)
    return when (val msg = VerifyEmailRequestValidator().validate(request)) {
      is InvalidInput -> HttpResponse.badRequest<UserDTO>().status(
        HttpStatus.BAD_REQUEST,
        msg.reason,
      )
      else -> {
        when (verifyUserEmail.execute(request.id, request.otk)) {
          is VerifyUserEmail.Companion.VerifyUserEmailResult.ConfirmedEmail -> {
            emailConfirmationKafkaClient.confirmEmail(
              UserEmailConfirmationNotification(request.id, USER_CONFIRMATION_KEY).toJson(),
            )
            HttpResponse.ok()
          }
          else -> HttpResponse.notFound()
        }
      }
    }
  }
}
