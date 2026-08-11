package com.munchies.restaurant.infrastructure.adapter.dto

import kotlin.js.JsExport
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsExport
@Serializable
object ValidityType {
  const val PERIOD = "PERIOD"
  const val YEARLY = "YEARLY"
  const val WEEKLY = "WEEKLY"
  const val HOURS = "HOURS"
  const val FROM = "FROM"
  const val UNTIL = "UNTIL"
  const val ALWAYS = "ALWAYS"
}

@JsExport
@Serializable
class ValidityDto(
  val type: String,
  val value: Map<String, String> = emptyMap(),
) {
  fun toJson(): String = Json.encodeToString(this)
}
