package com.munchies.user.infrastructure.adapter.outbound.response

import com.munchies.commons.infrastructure.adapter.JsonEncodable
import com.munchies.user.infrastructure.adapter.dto.UserDTO
import kotlin.js.JsExport
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
class DeleteUserResponse(val user: UserDTO) :
  JsonEncodable() {
  override fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun deleteUserResponseFromJson(json: String): DeleteUserResponse = Json.decodeFromString(json)
