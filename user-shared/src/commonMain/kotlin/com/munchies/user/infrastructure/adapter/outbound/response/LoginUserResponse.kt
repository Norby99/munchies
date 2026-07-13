package com.munchies.user.infrastructure.adapter.outbound.response

import com.munchies.commons.domain.port.AuthRole
import com.munchies.commons.infrastructure.adapter.WebResponse
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
@SerialName("LoginUserResponse")
class LoginUserResponse(
  override val result: LoginUserResult,
  override val code: Int,
) : WebResponse<LoginUserResult>() {
  override fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun loginUserResponseFromJson(json: String): LoginUserResponse = Json.decodeFromString(json)

@JsExport
@Serializable
sealed class LoginUserResult {
  abstract val type: String
}

@JsExport
@Serializable
@SerialName("LoginUserSuccess")
class LoginUserSuccess(val id: String, val role: AuthRole) : LoginUserResult() {
  override val type: String
    get() = "LoginUserSuccess"
}

@JsExport
@Serializable
@SerialName("LoginUserFailure")
class LoginUserFailure(val reason: String) : LoginUserResult() {
  override val type: String
    get() = "LoginUserFailure"
}
