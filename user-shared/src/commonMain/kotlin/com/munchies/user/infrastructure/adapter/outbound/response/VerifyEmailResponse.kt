package com.munchies.user.infrastructure.adapter.outbound.response

import com.munchies.commons.infrastructure.adapter.WebResponse
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
@SerialName("VerifyEmailResponse")
open class VerifyEmailResponse(
  override val result: String,
  override val code: Int = 200,
) : WebResponse<String>() {
  override fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun verifyEmailResponseFromJson(jsonString: String): VerifyEmailResponse =
  Json.decodeFromString(jsonString)
