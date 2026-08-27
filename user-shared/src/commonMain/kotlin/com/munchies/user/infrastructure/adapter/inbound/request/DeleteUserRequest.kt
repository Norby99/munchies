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
@SerialName("DeleteUserRequest")
data class DeleteUserRequest(
  val userId: String,
) : AuthenticatedRequest<DeleteUserRequest>, JsonEncodable() {
  override fun toJson(): String = wireJson.encodeToString(this)
  override fun addId(userId: String): DeleteUserRequest = this.copy(userId = userId)
}

@JsExport
fun deleteUserRequestFromJson(json: String): DeleteUserRequest = Json.decodeFromString(json)
