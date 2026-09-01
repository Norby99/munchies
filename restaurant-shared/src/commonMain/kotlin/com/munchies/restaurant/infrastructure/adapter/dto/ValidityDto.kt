package com.munchies.restaurant.infrastructure.adapter.dto

import com.munchies.commons.infrastructure.adapter.wireJson
import kotlin.js.JsExport
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

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
  fun toJson(): String = wireJson.encodeToString(this)
}
