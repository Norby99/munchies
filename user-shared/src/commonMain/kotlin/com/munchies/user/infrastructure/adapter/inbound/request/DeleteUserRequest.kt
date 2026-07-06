package com.munchies.user.infrastructure.adapter.inbound.request

import kotlin.js.JsExport
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
data class DeleteUserRequest(
  val userId: String,
) {
  fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun deleteUserRequestFromJson(json: String): DeleteUserRequest = Json.decodeFromString(json)
