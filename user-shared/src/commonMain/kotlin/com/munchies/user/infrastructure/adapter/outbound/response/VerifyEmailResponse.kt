package com.munchies.user.infrastructure.adapter.outbound.response

import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
class VerifyEmailResponse(
  val result: VerifyEmailResult,
  val code: Int,
) {
  fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun verifyEmailResponseFromJson(jsonString: String): VerifyEmailResponse =
  Json.decodeFromString(jsonString)

@JsExport
@Serializable
sealed class VerifyEmailResult {
  abstract val type: String
}

@JsExport
@Serializable
@SerialName("VerifyEmailSuccess")
class VerifyEmailSuccess(val msg: String) : VerifyEmailResult() {
  override val type: String
    get() = "VerifyEmailSuccess"
}

@JsExport
@Serializable
@SerialName("VerifyEmailFailure")
class VerifyEmailFailure(val reason: String) : VerifyEmailResult() {
  override val type: String
    get() = "VerifyEmailFailure"
}
