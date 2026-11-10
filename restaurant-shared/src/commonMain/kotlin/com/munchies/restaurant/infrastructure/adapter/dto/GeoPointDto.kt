package com.munchies.restaurant.infrastructure.adapter.dto

import com.munchies.commons.infrastructure.adapter.wireJson
import kotlin.js.JsExport
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

/** Wire form of a WGS84 coordinate: plain JSON numbers, latitude first. */
@JsExport
@Serializable
class GeoPointDto(
  val lat: Double,
  val lng: Double,
) {
  fun toJson(): String = wireJson.encodeToString(this)
}
