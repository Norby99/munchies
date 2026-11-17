package com.munchies.restaurant.infrastructure.adapter.dto

import com.munchies.commons.infrastructure.adapter.wireJson
import kotlin.js.JsExport
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

/**
 * Wire form of the restaurant's capacity policy. [opening] and [closing] are "HH:mm"
 * strings in restaurant-local time; distances are straight-line kilometres.
 */
@JsExport
@Serializable
class CapacityPolicyDto(
  val slotMinutes: Int,
  val maxCapacityUnits: Int,
  val opening: String,
  val closing: String,
  val prepLeadMinutes: Int,
  val maxAdvanceDays: Int,
  val maxTripsPerSlot: Int,
  val maxTripKmBudget: Double,
  val maxDetourKm: Double,
) {
  fun toJson(): String = wireJson.encodeToString(this)
}
