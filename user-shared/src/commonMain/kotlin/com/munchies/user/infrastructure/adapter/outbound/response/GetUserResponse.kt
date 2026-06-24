package com.munchies.user.infrastructure.adapter.outbound.response

import com.munchies.user.infrastructure.adapter.dto.UserDTO
import kotlin.js.JsExport
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
data class GetUserResponse(
  val result: GetUserResult,
) {
  fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun getUserResponseFromJson(json: String): GetUserResponse = Json.decodeFromString(json)

@JsExport
@Serializable
sealed class GetUserResult {
  abstract val type: String
}

@JsExport
@Serializable
class GetUserSuccess(val user: UserDTO) : GetUserResult() {
  override val type: String
    get() = "Success"
}

@JsExport
@Serializable
class GetUserFailure(val reason: String) : GetUserResult() {
  override val type: String
    get() = "Failure"
}
