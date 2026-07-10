package com.munchies.user.infrastructure.adapter.outbound.response

import com.munchies.commons.domain.port.AuthRole
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
class LoginUserResponse(val result: LoginUserResult, val code: Int) {
  fun toJson(): String = Json.encodeToString(this)
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
