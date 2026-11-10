package com.munchies.restaurant.infrastructure.adapter.outbound.mongo.document

import io.micronaut.serde.annotation.Serdeable

/** Mongo form of the capacity policy. Times are stored as "HH:mm" strings, like ValidityData. */
@Serdeable
data class CapacityPolicyData(
  val slotMinutes: Int,
  val maxCapacityUnits: Int,
  val opening: String,
  val closing: String,
  val prepLeadMinutes: Int,
  val maxAdvanceDays: Int,
  val maxTripsPerSlot: Int,
  val maxTripKmBudget: Double,
  val maxDetourKm: Double,
)
