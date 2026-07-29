package com.munchies.user.infrastructure.adapter.outbound.response

import com.munchies.commons.infrastructure.adapter.JsonEncodable
import com.munchies.user.infrastructure.adapter.dto.UserDTO
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
@SerialName("RegisterUserResponse")
open class RegisterUserResponse(
  val user: UserDTO,
) : JsonEncodable() {
  override fun toJson(): String = Json.encodeToString(this as RegisterUserResponse)
}

@JsExport
fun registerUserResponseFromJson(json: String): RegisterUserResponse =
  (Json.decodeFromString(json) as RegisterUserResponse)
