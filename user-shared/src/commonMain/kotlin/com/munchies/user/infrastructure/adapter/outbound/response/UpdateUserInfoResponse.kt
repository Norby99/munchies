package com.munchies.user.infrastructure.adapter.outbound.response

import com.munchies.commons.infrastructure.adapter.WebResponse
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
@SerialName("UpdateUserInfoResponse")
class UpdateUserInfoResponse(
  override val result: UpdateUserInfoResult,
  override val code: Int,
) : WebResponse<UpdateUserInfoResult>() {
  override fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun updateUserInfoResponseFromJson(json: String): UpdateUserInfoResponse =
  Json.decodeFromString(json)

@JsExport
@Serializable
sealed class UpdateUserInfoResult {
  abstract val type: String
}

@JsExport
@Serializable
@SerialName("UpdateUserInfoSuccess")
class UpdateUserInfoSuccess(val msg: String) : UpdateUserInfoResult() {
  override val type: String
    get() = "UpdateUserInfoSuccess"
}

@JsExport
@Serializable
@SerialName("UpdateUserInfoFailure")
class UpdateUserInfoFailure(val reason: String) : UpdateUserInfoResult() {
  override val type: String
    get() = "UpdateUserInfoFailure"
}
