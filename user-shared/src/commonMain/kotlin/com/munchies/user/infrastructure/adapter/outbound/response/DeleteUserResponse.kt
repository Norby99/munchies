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
class DeleteUserResponse(override val result: DeleteUserResult, override val code: Int) :
  WebResponse<DeleteUserResult>() {
  override fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun deleteUserResponseFromJson(json: String): DeleteUserResponse = Json.decodeFromString(json)

@JsExport
@Serializable
sealed class DeleteUserResult {
  abstract val type: String
}

@JsExport
@Serializable
@SerialName("DeleteUserSuccess")
class DeleteUserSuccess(val user: UserDTO) : DeleteUserResult() {
  override val type: String
    get() = "DeleteUserSuccess"
}

@JsExport
@Serializable
@SerialName("DeleteUserFailure")
class DeleteUserFailure(val reason: String) : DeleteUserResult() {
  override val type: String
    get() = "DeleteUserFailure"
}
