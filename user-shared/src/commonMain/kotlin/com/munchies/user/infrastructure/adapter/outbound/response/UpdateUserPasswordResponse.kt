package com.munchies.user.infrastructure.adapter.outbound.response

import com.munchies.commons.infrastructure.adapter.WebResponse
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
@SerialName("UpdateUserPasswordResponse")
class UpdateUserPasswordResponse(
  override val result: UpdateUserPasswordResult,
  override val code: Int,
) : WebResponse<UpdateUserPasswordResult>() {
  override fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun updateUserPasswordResponseFromJson(json: String): UpdateUserPasswordResponse =
  Json.decodeFromString(json)

@JsExport
@Serializable
sealed class UpdateUserPasswordResult {
  abstract val type: String
}

@JsExport
@Serializable
@SerialName("UpdateUserPasswordSuccess")
class UpdateUserPasswordSuccess(val msg: String) : UpdateUserPasswordResult() {
  override val type: String
    get() = "UpdateUserPasswordSuccess"
}

@JsExport
@Serializable
@SerialName("UpdateUserPasswordFailure")
class UpdateUserPasswordFailure(val reason: String) : UpdateUserPasswordResult() {
  override val type: String
    get() = "UpdateUserPasswordFailure"
}
