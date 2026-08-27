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
@SerialName("UpdateUserPasswordRequest")
data class UpdateUserPasswordRequest(
  val id: String,
  val username: String = "",
  val email: String = "",
  val oldHashedPassword: String,
  val newPassword: String,
) : AuthenticatedRequest<UpdateUserPasswordRequest>, JsonEncodable() {
  override fun toJson(): String = wireJson.encodeToString(this)
  override fun addId(userId: String): UpdateUserPasswordRequest = this.copy(id = userId)
}

@JsExport
fun updateUserPasswordRequestFromJson(json: String): UpdateUserPasswordRequest =
  Json.decodeFromString(json)
