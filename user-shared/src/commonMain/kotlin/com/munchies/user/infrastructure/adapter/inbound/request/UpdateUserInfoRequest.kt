package com.munchies.user.infrastructure.adapter.inbound.request

import com.munchies.commons.infrastructure.adapter.AuthenticatedRequest
import com.munchies.commons.infrastructure.adapter.JsonEncodable
import com.munchies.commons.infrastructure.adapter.wireJson
import com.munchies.user.infrastructure.adapter.dto.UserDTO
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
@SerialName("UpdateUserInfoRequest")
data class UpdateUserInfoRequest(
  val user: UserDTO,
) : AuthenticatedRequest<UpdateUserInfoRequest>, JsonEncodable() {
  override fun toJson(): String = wireJson.encodeToString(this)
  override fun addId(userId: String): UpdateUserInfoRequest =
    this.copy(user = this.user.copy(id = userId))
}

@JsExport
fun updateUserInfoRequestFromJson(json: String): UpdateUserInfoRequest = Json.decodeFromString(json)
