package com.munchies.user.infrastructure.adapter.inbound

import com.munchies.commons.infrastructure.adapter.API
import com.munchies.user.infrastructure.adapter.dto.UserDTO
import com.munchies.user.infrastructure.adapter.inbound.request.*
import com.munchies.user.infrastructure.adapter.inbound.web.config.UserServiceConfig
import com.munchies.user.infrastructure.adapter.outbound.response.GetUserResult
import com.munchies.user.infrastructure.adapter.outbound.response.LoginUserResponse
import com.munchies.user.infrastructure.adapter.outbound.response.RegisterUserResult
import kotlin.js.Promise

@JsExport
abstract class JsGetUserAPI : UserAPI.GetUserAPI<Promise<GetUserResult>>, API() {
  override fun getPath(): String = UserServiceConfig.SERVICE_PATH
  override fun getPort(): Int = UserServiceConfig.SERVICE_PORT
  abstract override fun getUser(id: String): Promise<GetUserResult>
}

@JsExport
abstract class JsRegisterUserAPI : UserAPI.RegisterUserAPI<Promise<RegisterUserResult>>, API() {
  override fun getPath(): String =
    UserServiceConfig.SERVICE_PATH + UserServiceConfig.REGISTER_USER_PATH
  override fun getPort(): Int = UserServiceConfig.SERVICE_PORT
  abstract override fun registerUser(request: RegisterUserRequest): Promise<RegisterUserResult>
}

@JsExport
abstract class JsLoginUserAPI : UserAPI.LoginUserAPI<LoginUserResponse>, API() {
  override fun getPath(): String =
    UserServiceConfig.SERVICE_PATH + UserServiceConfig.LOGIN_USER_PATH
  override fun getPort(): Int = UserServiceConfig.SERVICE_PORT
  abstract override fun loginUser(request: LoginUserRequest): LoginUserResponse
}

@JsExport
abstract class JsUpdateUserPasswordAPI :
  UserAPI.UpdateUserPasswordAPI<UpdateUserPasswordRequest, UserDTO>, API() {
  override fun getPath(): String =
    UserServiceConfig.SERVICE_PATH + UserServiceConfig.UPDATE_USER_PASSWORD_PATH
  override fun getPort(): Int = UserServiceConfig.SERVICE_PORT
  abstract override fun updateUserPassword(request: UpdateUserPasswordRequest): UserDTO
}

@JsExport
abstract class JsUpdateUserInfoAPI :
  UserAPI.UpdateUserInfoAPI<UpdateUserInfoRequest, UserDTO>, API() {
  override fun getPath(): String =
    UserServiceConfig.SERVICE_PATH + UserServiceConfig.UPDATE_USER_INFO_PATH
  override fun getPort(): Int = UserServiceConfig.SERVICE_PORT
  abstract override fun updateUserInfo(request: UpdateUserInfoRequest): UserDTO
}

@JsExport
abstract class JsDeleteUserAPI : UserAPI.DeleteUserAPI<DeleteUserRequest, UserDTO>, API() {
  override fun getPath(): String =
    UserServiceConfig.SERVICE_PATH + UserServiceConfig.DELETE_USER_PATH
  override fun getPort(): Int = UserServiceConfig.SERVICE_PORT
  abstract override fun deleteUser(request: DeleteUserRequest): UserDTO
}

@JsExport
abstract class JsEmailVerificationAPI : UserAPI.EmailVerificationAPI<UserDTO>, API() {
  override fun getPath(): String =
    UserServiceConfig.SERVICE_PATH + UserServiceConfig.VERIFY_EMAIL_PATH
  override fun getPort(): Int = UserServiceConfig.SERVICE_PORT
  abstract override fun verifyEmail(id: String, otk: String): UserDTO
}
