package com.munchies.user.infrastructure.adapter.outbound.response

import com.munchies.commons.infrastructure.adapter.WebResponse
import com.munchies.user.infrastructure.adapter.dto.UserDTO
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
@SerialName("RegisterUserResponse")
open class RegisterUserResponse(
  override val result: RegisterUserResult,
  override val code: Int,
) : WebResponse<RegisterUserResult>() {
  override fun toJson(): String = Json.encodeToString(this as RegisterUserResponse)
}

@JsExport
fun registerUserResponseFromJson(json: String): RegisterUserResponse =
  (Json.decodeFromString(json) as RegisterUserResponse)

@JsExport
@Serializable
@SerialName("RegisterUserResult")
sealed class RegisterUserResult {
  abstract val type: String
}

@JsExport
@Serializable
@SerialName("RegisterUserSuccess")
class RegisterUserSuccess(val user: UserDTO) : RegisterUserResult() {
  override val type: String
    get() = "RegisterUserSuccess"
}

@JsExport
@Serializable
@SerialName("RegisterUserFailure")
class RegisterUserFailure(val reason: String) : RegisterUserResult() {
  override val type: String
    get() = "RegisterUserFailure"
}
