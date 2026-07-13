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
open class GetUserResponse(
  override val result: GetUserResult,
  override val code: Int,
) : WebResponse<GetUserResult>() {
  override fun toJson(): String = Json.encodeToString(this)
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
@SerialName("GetUserSuccess")
class GetUserSuccess(val user: UserDTO) : GetUserResult() {
  override val type: String
    get() = "GetUserSuccess"
}

@JsExport
@Serializable
@SerialName("GetUserFailure")
class GetUserFailure(val reason: String) : GetUserResult() {
  override val type: String
    get() = "GetUserFailure"
}
