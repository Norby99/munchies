package com.munchies.user.infrastructure.adapter.outbound.response

import com.munchies.commons.infrastructure.adapter.WebResponse
import com.munchies.user.infrastructure.adapter.dto.UserDTO
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
@SerialName("DeleteUserResponse")
open class DeleteUserResponse(
  override val result: UserDTO,
  override val code: Int = 200,
) : WebResponse<UserDTO>() {
  override fun toJson(): String = Json.encodeToString(this)

  val user: UserDTO get() = result
}

@JsExport
fun deleteUserResponseFromJson(json: String): DeleteUserResponse = Json.decodeFromString(json)
