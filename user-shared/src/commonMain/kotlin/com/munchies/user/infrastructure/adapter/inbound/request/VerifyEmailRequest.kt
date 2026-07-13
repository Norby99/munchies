package com.munchies.user.infrastructure.adapter.inbound.request

import com.munchies.commons.infrastructure.adapter.JsonEncodable
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
@SerialName("VerifyEmailRequest")
data class VerifyEmailRequest(val id: String, val otk: String) : JsonEncodable() {
  override fun toJson() = Json.encodeToString(this)
}

@JsExport
fun verifyEmailRequestFromJson(json: String): VerifyEmailRequest = Json.decodeFromString(json)
