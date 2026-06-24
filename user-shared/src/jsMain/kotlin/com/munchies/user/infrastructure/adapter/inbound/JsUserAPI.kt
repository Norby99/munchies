package com.munchies.user.infrastructure.adapter.inbound

import com.munchies.commons.infrastructure.adapter.API
import com.munchies.user.infrastructure.adapter.dto.UserDTO
import com.munchies.user.infrastructure.adapter.inbound.request.*
import com.munchies.user.infrastructure.adapter.inbound.web.config.UserServiceConfig
import com.munchies.user.infrastructure.adapter.outbound.response.GetUserResult

@JsExport
abstract class JsGetUserAPI : UserAPI.GetUserAPI<GetUserResult>, API() {
  override fun getPath(): String = UserServiceConfig.SERVICE_PATH
  override fun getPort(): Int = UserServiceConfig.SERVICE_PORT
  abstract override fun getUser(id: String): GetUserResult
}

@JsExport
abstract class JsRegisterUserAPI : UserAPI.RegisterUserAPI<UserDTO>, API() {
  override fun getPath(): String = UserServiceConfig.REGISTER_USER_PATH
  override fun getPort(): Int = UserServiceConfig.SERVICE_PORT
  abstract override fun registerUser(request: RegisterUserRequest): UserDTO
}

@JsExport
abstract class JsLoginUserAPI : UserAPI.LoginUserAPI<LoginUserRequest, UserDTO> {
  abstract override fun loginUser(request: LoginUserRequest): UserDTO
}

@JsExport
abstract class JsUpdateUserPasswordAPI :
  UserAPI.UpdateUserPasswordAPI<UpdateUserPasswordRequest, UserDTO> {
  abstract override fun updateUserPassword(request: UpdateUserPasswordRequest): UserDTO
}

@JsExport
abstract class JsUpdateUserInfoAPI : UserAPI.UpdateUserInfoAPI<UpdateUserInfoRequest, UserDTO> {
  abstract override fun updateUserInfo(request: UpdateUserInfoRequest): UserDTO
}

@JsExport
abstract class JsDeleteUserAPI : UserAPI.DeleteUserAPI<DeleteUserRequest, UserDTO> {
  abstract override fun deleteUser(request: DeleteUserRequest): UserDTO
}

@JsExport
abstract class JsEmailVerificationAPI : UserAPI.EmailVerificationAPI<UserDTO> {
  abstract override fun verifyEmail(id: String, otk: String): UserDTO
}
