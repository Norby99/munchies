package com.munchies.user.infrastructure.adapter.inbound.request

import com.munchies.commons.infrastructure.adapter.JsonEncodable
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
) : JsonEncodable() {
  override fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun deleteUserRequestFromJson(json: String): DeleteUserRequest = Json.decodeFromString(json)
