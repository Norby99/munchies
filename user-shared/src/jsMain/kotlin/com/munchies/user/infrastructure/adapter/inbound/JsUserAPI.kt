package com.munchies.user.infrastructure.adapter.inbound

import com.munchies.commons.infrastructure.adapter.API
import com.munchies.commons.infrastructure.adapter.HttpMethod
import com.munchies.user.infrastructure.adapter.dto.UserDTO
import com.munchies.user.infrastructure.adapter.inbound.request.*
import com.munchies.user.infrastructure.adapter.inbound.web.config.UserServiceConfig
import com.munchies.user.infrastructure.adapter.outbound.response.*
import kotlin.js.Promise

@JsExport
abstract class JsGetUserAPI : UserAPI.GetUserAPI<Promise<GetUserResponse>>, API() {
  override fun getMethod(): HttpMethod = HttpMethod.GET
  override fun getPath(): String = UserServiceConfig.SERVICE_PATH
  override fun getPort(): Int = UserServiceConfig.SERVICE_PORT
  abstract override fun getUser(id: String): Promise<GetUserResponse>
}

@JsExport
abstract class JsRegisterUserAPI : UserAPI.RegisterUserAPI<Promise<RegisterUserResponse>>, API() {
  override fun getPath(): String =
    UserServiceConfig.SERVICE_PATH + UserServiceConfig.REGISTER_USER_PATH
  override fun getPort(): Int = UserServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.POST
  abstract override fun registerUser(request: RegisterUserRequest): Promise<RegisterUserResponse>
}

@JsExport
abstract class JsLoginUserAPI : UserAPI.LoginUserAPI<Promise<LoginUserResponse>>, API() {
  override fun getPath(): String =
    UserServiceConfig.SERVICE_PATH + UserServiceConfig.LOGIN_USER_PATH
  override fun getPort(): Int = UserServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.POST
  abstract override fun loginUser(request: LoginUserRequest): Promise<LoginUserResponse>
}

@JsExport
abstract class JsUpdateUserPasswordAPI :
  UserAPI.UpdateUserPasswordAPI<Promise<UpdateUserPasswordResponse>>, API() {
  override fun getPath(): String =
    UserServiceConfig.SERVICE_PATH + UserServiceConfig.UPDATE_USER_PASSWORD_PATH
  override fun getPort(): Int = UserServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.PATCH
  abstract override fun updateUserPassword(
    request: UpdateUserPasswordRequest,
  ): Promise<UpdateUserPasswordResponse>
}

@JsExport
abstract class JsUpdateUserInfoAPI :
  UserAPI.UpdateUserInfoAPI<Promise<UpdateUserInfoResponse>>, API() {
  override fun getPath(): String =
    UserServiceConfig.SERVICE_PATH + UserServiceConfig.UPDATE_USER_INFO_PATH
  override fun getPort(): Int = UserServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.PATCH
  abstract override fun updateUserInfo(
    request: UpdateUserInfoRequest,
  ): Promise<UpdateUserInfoResponse>
}

@JsExport
abstract class JsDeleteUserAPI : UserAPI.DeleteUserAPI<Promise<DeleteUserResponse>>, API() {
  override fun getPath(): String =
    UserServiceConfig.SERVICE_PATH + UserServiceConfig.DELETE_USER_PATH
  override fun getPort(): Int = UserServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.DELETE
  abstract override fun deleteUser(request: DeleteUserRequest): Promise<DeleteUserResponse>
}

@JsExport
abstract class JsEmailVerificationAPI : UserAPI.EmailVerificationAPI<UserDTO>, API() {
  override fun getPath(): String =
    UserServiceConfig.SERVICE_PATH + UserServiceConfig.VERIFY_EMAIL_PATH
  override fun getPort(): Int = UserServiceConfig.SERVICE_PORT
  override fun getMethod(): HttpMethod = HttpMethod.GET
  abstract override fun verifyEmail(id: String, otk: String): UserDTO
}
