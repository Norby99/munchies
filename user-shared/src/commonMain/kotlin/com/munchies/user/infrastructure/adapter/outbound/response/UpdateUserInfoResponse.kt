package com.munchies.user.infrastructure.adapter.outbound.response

import com.munchies.commons.infrastructure.adapter.JsonEncodable
import kotlin.js.JsExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
@SerialName("UpdateUserInfoResponse")
class UpdateUserInfoResponse(
  val msg: String,
) : JsonEncodable() {
  override fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun updateUserInfoResponseFromJson(json: String): UpdateUserInfoResponse =
  Json.decodeFromString(json)
