package com.munchies.restaurant.domain.valueobject.restaurant

import java.time.LocalTime

/**
 * Output configuration consumed by scheduler-service: how much kitchen and delivery
 * throughput a slot can absorb, and when customers may schedule orders.
 *
 * Kitchen throughput is expressed in weight units (an order's weight is its total item
 * quantity), not in order count. Delivery throughput is expressed as driver trips, and a
 * trip's length in kilometres. Distances are straight-line, not road distances.
 */
data class CapacityPolicy(
  val slotMinutes: Int,
  val maxCapacityUnits: Int,
  val opening: LocalTime,
  val closing: LocalTime,
  val prepLeadMinutes: Int,
  val maxAdvanceDays: Int,
  val maxTripsPerSlot: Int,
  val maxTripKmBudget: Double,
  val maxDetourKm: Double,
) {
  init {
    require(slotMinutes > 0) { "slotMinutes must be positive" }
    require(maxCapacityUnits > 0) { "maxCapacityUnits must be positive" }
    require(opening.isBefore(closing)) { "opening must be before closing" }
    require(prepLeadMinutes >= 0) { "prepLeadMinutes cannot be negative" }
    require(maxAdvanceDays > 0) { "maxAdvanceDays must be positive" }
    require(maxTripsPerSlot >= 1) { "maxTripsPerSlot must be at least 1" }
    require(maxTripKmBudget.isFinite() && maxTripKmBudget > 0) {
      "maxTripKmBudget must be a positive finite number"
    }
    require(maxDetourKm.isFinite() && maxDetourKm >= 0) {
      "maxDetourKm must be a non-negative finite number"
    }
  }
}
