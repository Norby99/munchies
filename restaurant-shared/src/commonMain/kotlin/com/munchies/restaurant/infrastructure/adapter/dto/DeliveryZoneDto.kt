package com.munchies.restaurant.infrastructure.adapter.dto

import com.munchies.commons.infrastructure.adapter.wireJson
import kotlin.js.JsExport
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

/**
 * Wire form of a delivery zone: an area whose addresses scheduler-service may batch on the same
 * driver trip.
 *
 * @property id Stable identifier, unique within the restaurant. Left empty by the client for a
 * zone drawn since the last save: restaurant-service generates one.
 * @property name The label the manager gives the zone, unique within the restaurant.
 * @property boundary The outer ring of the polygon in drawing order, not closed (the last vertex
 * joins the first). At least three distinct vertices; holes are not supported.
 */
@JsExport
@Serializable
class DeliveryZoneDto(
  val id: String = "",
  val name: String,
  val boundary: Array<GeoPointDto>,
) {
  fun toJson(): String = wireJson.encodeToString(this)
}
