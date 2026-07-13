package com.munchies.user.infrastructure.adapter.inbound

import com.munchies.commons.domain.port.AuthRole
import com.munchies.commons.infrastructure.adapter.API
import com.munchies.commons.infrastructure.adapter.HttpMethod
import com.munchies.user.infrastructure.adapter.inbound.request.*
import com.munchies.user.infrastructure.adapter.inbound.web.config.UserServiceConfig
import com.munchies.user.infrastructure.adapter.outbound.response.*
import kotlin.js.Promise

@JsExport
abstract class JsGetUserAPI : UserAPI.GetUserAPI<Promise<GetUserResponse>>,
  API<GetUserRequest, GetUserResponse, GetUserResult, GetUserSuccess, GetUserFailure>() {
  override fun getMethod(): HttpMethod = HttpMethod.GET
  override fun getPath(): String = UserServiceConfig.SERVICE_PATH
  override fun getPort(): Int = UserServiceConfig.SERVICE_PORT
  override fun getRequiredAuthRole(): AuthRole = AuthRole.CUSTOMER
  abstract override fun getUser(id: String): Promise<GetUserResponse>
  override fun generateFailure(reason: String): GetUserFailure = GetUserFailure(reason)
  override fun generateResponse(result: GetUserResult, code: Int): GetUserResponse =
    GetUserResponse(result, code)

  override fun parseRequest(json: String): GetUserRequest = getUserRequestFromJson(json)
  override fun parseResponse(json: String): GetUserResponse = getUserResponseFromJson(json)
}

@JsExport
abstract class JsRegisterUserAPI : UserAPI.RegisterUserAPI<Promise<RegisterUserResponse>>,
  API<
    RegisterUserRequest,
    RegisterUserResponse,
    RegisterUserResult,
    RegisterUserSuccess,
    RegisterUserFailure,
    >() {
  override fun getPath(): String =
    UserServiceConfig.SERVICE_PATH + UserServiceConfig.REGISTER_USER_PATH

  override fun getPort(): Int = UserServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.POST
  override fun getRequiredAuthRole(): AuthRole = AuthRole.CUSTOMER
  abstract override fun registerUser(request: RegisterUserRequest): Promise<RegisterUserResponse>
  override fun generateFailure(reason: String): RegisterUserFailure = RegisterUserFailure(reason)
  override fun generateResponse(result: RegisterUserResult, code: Int): RegisterUserResponse =
    RegisterUserResponse(result, code)

  override fun parseRequest(json: String): RegisterUserRequest = registerUserRequestFromJson(json)
  override fun parseResponse(json: String): RegisterUserResponse =
    registerUserResponseFromJson(json)
}

@JsExport
abstract class JsLoginUserAPI : UserAPI.LoginUserAPI<Promise<LoginUserResponse>>,
  API<LoginUserRequest, LoginUserResponse, LoginUserResult, LoginUserSuccess, LoginUserFailure>() {
  override fun getPath(): String =
    UserServiceConfig.SERVICE_PATH + UserServiceConfig.LOGIN_USER_PATH

  override fun getPort(): Int = UserServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.POST
  override fun getRequiredAuthRole(): AuthRole = AuthRole.CUSTOMER
  abstract override fun loginUser(request: LoginUserRequest): Promise<LoginUserResponse>
  override fun generateFailure(reason: String): LoginUserFailure = LoginUserFailure(reason)
  override fun generateResponse(result: LoginUserResult, code: Int): LoginUserResponse =
    LoginUserResponse(result, code)

  override fun parseRequest(json: String): LoginUserRequest = loginUserRequestFromJson(json)
  override fun parseResponse(json: String): LoginUserResponse = loginUserResponseFromJson(json)
}

@JsExport
abstract class JsUpdateUserPasswordAPI :
  UserAPI.UpdateUserPasswordAPI<Promise<UpdateUserPasswordResponse>>,
  API<
    UpdateUserPasswordRequest,
    UpdateUserPasswordResponse,
    UpdateUserPasswordResult,
    UpdateUserPasswordSuccess,
    UpdateUserPasswordFailure,
    >() {
  override fun getPath(): String =
    UserServiceConfig.SERVICE_PATH + UserServiceConfig.UPDATE_USER_PASSWORD_PATH

  override fun getPort(): Int = UserServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.PATCH
  override fun getRequiredAuthRole(): AuthRole = AuthRole.CUSTOMER
  abstract override fun updateUserPassword(
    request: UpdateUserPasswordRequest,
  ): Promise<UpdateUserPasswordResponse>

  override fun generateFailure(reason: String): UpdateUserPasswordFailure =
    UpdateUserPasswordFailure(
      reason,
    )

  override fun generateResponse(
    result: UpdateUserPasswordResult,
    code: Int,
  ): UpdateUserPasswordResponse = UpdateUserPasswordResponse(result, code)

  override fun parseRequest(json: String): UpdateUserPasswordRequest =
    updateUserPasswordRequestFromJson(json)

  override fun parseResponse(json: String): UpdateUserPasswordResponse =
    updateUserPasswordResponseFromJson(json)
}

@JsExport
abstract class JsUpdateUserInfoAPI :
  UserAPI.UpdateUserInfoAPI<Promise<UpdateUserInfoResponse>>,
  API<
    UpdateUserInfoRequest,
    UpdateUserInfoResponse,
    UpdateUserInfoResult,
    UpdateUserInfoSuccess,
    UpdateUserInfoFailure,
    >() {
  override fun getPath(): String =
    UserServiceConfig.SERVICE_PATH + UserServiceConfig.UPDATE_USER_INFO_PATH

  override fun getPort(): Int = UserServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.PATCH
  override fun getRequiredAuthRole(): AuthRole = AuthRole.CUSTOMER
  abstract override fun updateUserInfo(
    request: UpdateUserInfoRequest,
  ): Promise<UpdateUserInfoResponse>

  override fun generateFailure(reason: String): UpdateUserInfoFailure =
    UpdateUserInfoFailure(reason)

  override fun generateResponse(result: UpdateUserInfoResult, code: Int): UpdateUserInfoResponse =
    UpdateUserInfoResponse(result, code)

  override fun parseRequest(json: String): UpdateUserInfoRequest =
    updateUserInfoRequestFromJson(json)

  override fun parseResponse(json: String): UpdateUserInfoResponse =
    updateUserInfoResponseFromJson(json)
}

@JsExport
abstract class JsDeleteUserAPI : UserAPI.DeleteUserAPI<Promise<DeleteUserResponse>>,
  API<DeleteUserRequest, DeleteUserResponse, DeleteUserResult, DeleteUserSuccess, DeleteUserFailure>
  () {
  override fun getPath(): String =
    UserServiceConfig.SERVICE_PATH + UserServiceConfig.DELETE_USER_PATH

  override fun getPort(): Int = UserServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.DELETE
  override fun getRequiredAuthRole(): AuthRole = AuthRole.CUSTOMER
  abstract override fun deleteUser(id: String): Promise<DeleteUserResponse>

  override fun generateFailure(reason: String): DeleteUserFailure = DeleteUserFailure(reason)

  override fun generateResponse(result: DeleteUserResult, code: Int): DeleteUserResponse =
    DeleteUserResponse(result, code)

  override fun parseRequest(json: String): DeleteUserRequest = deleteUserRequestFromJson(json)
  override fun parseResponse(json: String): DeleteUserResponse = deleteUserResponseFromJson(json)
}

@JsExport
abstract class JsEmailVerificationAPI :
  UserAPI.EmailVerificationAPI<Promise<VerifyEmailResponse>>,
  API<
    VerifyEmailRequest,
    VerifyEmailResponse,
    VerifyEmailResult,
    VerifyEmailSuccess,
    VerifyEmailFailure,
    >() {
  override fun getPath(): String =
    UserServiceConfig.SERVICE_PATH + UserServiceConfig.VERIFY_EMAIL_PATH

  override fun getPort(): Int = UserServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.GET
  override fun getRequiredAuthRole(): AuthRole = AuthRole.CUSTOMER
  abstract override fun verifyEmail(request: VerifyEmailRequest): Promise<VerifyEmailResponse>
  override fun generateFailure(reason: String): VerifyEmailFailure = VerifyEmailFailure(reason)
  override fun generateResponse(result: VerifyEmailResult, code: Int): VerifyEmailResponse =
    VerifyEmailResponse(result, code)

  override fun parseRequest(json: String): VerifyEmailRequest = verifyEmailRequestFromJson(json)
  override fun parseResponse(json: String): VerifyEmailResponse = verifyEmailResponseFromJson(json)
}
