package com.munchies.user.infrastructure.adapter.outbound.response

import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
open class RegisterUserResponse(val result: RegisterUserResult) {
  fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun registerUserResponseFromJson(json: String): RegisterUserResponse = Json.decodeFromString(json)

@Serializable
sealed class RegisterUserResult {
  abstract val type: String
}

@JsExport
@Serializable
@SerialName("RegisterUserSuccess")
class RegisterUserSuccess(val msg: String) : RegisterUserResult() {
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
