package com.munchies.restaurant.infrastructure.adapter.inbound.http

import kotlin.js.JsExport
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
class ErrorResponse(
  val error: String,
) {
  fun toJson(): String = Json.encodeToString(this)
}

@JsExport
fun errorResponseFromJson(json: String): ErrorResponse = Json.decodeFromString(json)
