package com.munchies.user.infrastructure.adapter.inbound.request

import com.munchies.commons.infrastructure.adapter.AuthenticatedRequest
import com.munchies.commons.infrastructure.adapter.JsonEncodable
import com.munchies.commons.infrastructure.adapter.wireJson
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
@SerialName("VerifyEmailRequest")
data class VerifyEmailRequest(val id: String, val otk: String) :
  AuthenticatedRequest<VerifyEmailRequest>, JsonEncodable() {
  override fun toJson() = wireJson.encodeToString(this)
  override fun addId(userId: String): VerifyEmailRequest = this.copy(id = userId)
}

@JsExport
fun verifyEmailRequestFromJson(json: String): VerifyEmailRequest = Json.decodeFromString(json)
