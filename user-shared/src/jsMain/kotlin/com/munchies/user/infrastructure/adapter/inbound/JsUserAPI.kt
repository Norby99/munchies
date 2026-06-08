package com.munchies.user.infrastructure.adapter.inbound

import com.munchies.user.infrastructure.adapter.dto.UserDTO
import com.munchies.user.infrastructure.adapter.inbound.request.LoginUserRequest
import com.munchies.user.infrastructure.adapter.inbound.request.RegisterUserRequest
import com.munchies.user.infrastructure.adapter.inbound.request.UpdateUserInfoRequest
import com.munchies.user.infrastructure.adapter.inbound.request.UpdateUserPasswordRequest

@JsExport
interface JsGetUserAPI : UserAPI.Companion.GetUserAPI<String, UserDTO>

@JsExport
interface JsRegisterUserAPI : UserAPI.Companion.RegisterUserAPI<RegisterUserRequest, UserDTO>

@JsExport
interface JsLoginUserAPI : UserAPI.Companion.LoginUserAPI<LoginUserRequest, UserDTO>

@JsExport
interface JsUpdateUserPasswordAPI :
  UserAPI.Companion.UpdateUserPasswordAPI<UpdateUserPasswordRequest, UserDTO>

@JsExport
interface JsUpdateUserInfoAPI : UserAPI.Companion.UpdateUserInfoAPI<UpdateUserInfoRequest, UserDTO>

@JsExport
interface JsDeleteUserAPI : UserAPI.Companion.DeleteUserAPI<UserDTO>

@JsExport
interface JsEmailVerificationAPI : UserAPI.Companion.EmailVerificationAPI<UserDTO>
