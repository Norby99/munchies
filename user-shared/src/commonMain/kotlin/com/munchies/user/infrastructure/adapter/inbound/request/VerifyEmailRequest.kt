package com.munchies.user.infrastructure.adapter.inbound.request

import kotlin.js.JsExport
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
data class VerifyEmailRequest(val id: String, val otk: String) {
  fun toJson() = Json.encodeToString(this)
}

@JsExport
fun verifyEmailRequestFromJson(json: String): VerifyEmailRequest = Json.decodeFromString(json)
