package com.munchies.restaurant.infrastructure.adapter.dto

import kotlin.js.JsExport
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
enum class ValidityType {
  PERIOD,
  YEARLY,
  WEEKLY,
  HOURS,
  FROM,
  UNTIL,
  ALWAYS,
}

@JsExport
@Serializable
class ValidityDto(
  val type: ValidityType,
  val value: Map<String, String> = emptyMap(),
) {
  fun toJson(): String = Json.encodeToString(this)
}
