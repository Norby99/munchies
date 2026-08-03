package com.munchies.user.infrastructure.adapter.inbound

import com.munchies.commons.domain.port.AuthRole
import com.munchies.commons.infrastructure.adapter.HttpMethod
import com.munchies.commons.infrastructure.adapter.SimpleAPI
import com.munchies.commons.infrastructure.adapter.WebResponse
import com.munchies.user.infrastructure.adapter.inbound.request.*
import com.munchies.user.infrastructure.adapter.inbound.web.config.UserServiceConfig
import com.munchies.user.infrastructure.adapter.outbound.response.*
import kotlin.js.Promise

@JsExport
abstract class JsGetUserAPI<E : WebResponse<Any>> : UserAPI.GetUserAPI<Promise<E>>,
  SimpleAPI<GetUserRequest, GetUserResponse>() {
  override fun getMethod(): HttpMethod = HttpMethod.GET
  override fun getPath(): String = UserServiceConfig.SERVICE_PATH
  override fun getPort(): Int = UserServiceConfig.SERVICE_PORT
  override fun getRequiredAuthRole(): AuthRole = AuthRole.CUSTOMER
  abstract override fun getUser(id: String): Promise<E>

  override fun parseRequest(json: String): GetUserRequest = getUserRequestFromJson(json)
  override fun parseResponse(json: String): GetUserResponse = getUserResponseFromJson(json)
}

@JsExport
abstract class JsRegisterUserAPI<E : WebResponse<Any>> :
  UserAPI.RegisterUserAPI<Promise<E>>,
  SimpleAPI<
    RegisterUserRequest,
    RegisterUserResponse,
    >() {
  override fun getPath(): String =
    UserServiceConfig.SERVICE_PATH + UserServiceConfig.REGISTER_USER_PATH

  override fun getPort(): Int = UserServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.POST
  override fun getRequiredAuthRole(): AuthRole = AuthRole.CUSTOMER
  abstract override fun registerUser(request: RegisterUserRequest): Promise<E>

  override fun parseRequest(json: String): RegisterUserRequest = registerUserRequestFromJson(json)
  override fun parseResponse(json: String): RegisterUserResponse =
    registerUserResponseFromJson(json)
}

@JsExport
abstract class JsLoginUserAPI<E : WebResponse<Any>> : UserAPI.LoginUserAPI<Promise<E>>,
  SimpleAPI<LoginUserRequest, LoginUserResponse>() {
  override fun getPath(): String =
    UserServiceConfig.SERVICE_PATH + UserServiceConfig.LOGIN_USER_PATH

  override fun getPort(): Int = UserServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.POST
  override fun getRequiredAuthRole(): AuthRole = AuthRole.CUSTOMER
  abstract override fun loginUser(request: LoginUserRequest): Promise<E>

  override fun parseRequest(json: String): LoginUserRequest = loginUserRequestFromJson(json)
  override fun parseResponse(json: String): LoginUserResponse = loginUserResponseFromJson(json)
}

@JsExport
abstract class JsUpdateUserPasswordAPI<E : WebResponse<Any>> :
  UserAPI.UpdateUserPasswordAPI<Promise<E>>,
  SimpleAPI<
    UpdateUserPasswordRequest,
    UpdateUserPasswordResponse,
    >() {
  override fun getPath(): String =
    UserServiceConfig.SERVICE_PATH + UserServiceConfig.UPDATE_USER_PASSWORD_PATH

  override fun getPort(): Int = UserServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.PATCH
  override fun getRequiredAuthRole(): AuthRole = AuthRole.CUSTOMER
  abstract override fun updateUserPassword(request: UpdateUserPasswordRequest): Promise<E>

  override fun parseRequest(json: String): UpdateUserPasswordRequest =
    updateUserPasswordRequestFromJson(json)

  override fun parseResponse(json: String): UpdateUserPasswordResponse =
    updateUserPasswordResponseFromJson(json)
}

@JsExport
abstract class JsUpdateUserInfoAPI<E : WebResponse<Any>> :
  UserAPI.UpdateUserInfoAPI<Promise<E>>,
  SimpleAPI<
    UpdateUserInfoRequest,
    UpdateUserInfoResponse,
    >() {
  override fun getPath(): String =
    UserServiceConfig.SERVICE_PATH + UserServiceConfig.UPDATE_USER_INFO_PATH

  override fun getPort(): Int = UserServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.PATCH
  override fun getRequiredAuthRole(): AuthRole = AuthRole.CUSTOMER
  abstract override fun updateUserInfo(request: UpdateUserInfoRequest): Promise<E>

  override fun parseRequest(json: String): UpdateUserInfoRequest =
    updateUserInfoRequestFromJson(json)

  override fun parseResponse(json: String): UpdateUserInfoResponse =
    updateUserInfoResponseFromJson(json)
}

@JsExport
abstract class JsDeleteUserAPI<E : WebResponse<Any>> :
  UserAPI.DeleteUserAPI<Promise<E>>,
  SimpleAPI<DeleteUserRequest, DeleteUserResponse>
  () {
  override fun getPath(): String =
    UserServiceConfig.SERVICE_PATH

  override fun getPort(): Int = UserServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.DELETE
  override fun getRequiredAuthRole(): AuthRole = AuthRole.CUSTOMER
  abstract override fun deleteUser(id: String): Promise<E>

  override fun parseRequest(json: String): DeleteUserRequest = deleteUserRequestFromJson(json)
  override fun parseResponse(json: String): DeleteUserResponse = deleteUserResponseFromJson(json)
}

@JsExport
abstract class JsEmailVerificationAPI<E : WebResponse<Any>> :
  UserAPI.EmailVerificationAPI<Promise<E>>,
  SimpleAPI<
    VerifyEmailRequest,
    VerifyEmailResponse,
    >() {
  override fun getPath(): String =
    UserServiceConfig.SERVICE_PATH + UserServiceConfig.VERIFY_EMAIL_PATH

  override fun getPort(): Int = UserServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.GET
  override fun getRequiredAuthRole(): AuthRole = AuthRole.CUSTOMER
  abstract override fun verifyEmail(request: VerifyEmailRequest): Promise<E>

  override fun parseRequest(json: String): VerifyEmailRequest = verifyEmailRequestFromJson(json)
  override fun parseResponse(json: String): VerifyEmailResponse = verifyEmailResponseFromJson(json)
}
