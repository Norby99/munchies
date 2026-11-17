package com.munchies.restaurant.domain.valueobject.restaurant

import java.time.LocalTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CapacityPolicyTest {

  private fun validPolicy() = CapacityPolicy(
    slotMinutes = 30,
    maxCapacityUnits = 20,
    opening = LocalTime.of(11, 0),
    closing = LocalTime.of(22, 0),
    prepLeadMinutes = 30,
    maxAdvanceDays = 7,
    maxTripsPerSlot = 2,
    maxTripKmBudget = 12.0,
    maxDetourKm = 0.5,
  )

  @Test
  fun `should accept a fully valid policy`() {
    val policy = validPolicy()
    assertEquals(20, policy.maxCapacityUnits)
  }

  @Test
  fun `should accept zero prep lead and zero detour`() {
    validPolicy().copy(prepLeadMinutes = 0, maxDetourKm = 0.0)
  }

  @Test
  fun `should reject a non-positive slot length`() {
    assertThrows<IllegalArgumentException> { validPolicy().copy(slotMinutes = 0) }
  }

  @Test
  fun `should reject a non-positive kitchen capacity`() {
    assertThrows<IllegalArgumentException> { validPolicy().copy(maxCapacityUnits = 0) }
  }

  @Test
  fun `should reject opening that is not before closing`() {
    assertThrows<IllegalArgumentException> {
      validPolicy().copy(opening = LocalTime.of(22, 0), closing = LocalTime.of(11, 0))
    }
    assertThrows<IllegalArgumentException> {
      validPolicy().copy(opening = LocalTime.of(11, 0), closing = LocalTime.of(11, 0))
    }
  }

  @Test
  fun `should reject a negative prep lead`() {
    assertThrows<IllegalArgumentException> { validPolicy().copy(prepLeadMinutes = -1) }
  }

  @Test
  fun `should reject a non-positive advance window`() {
    assertThrows<IllegalArgumentException> { validPolicy().copy(maxAdvanceDays = 0) }
  }

  @Test
  fun `should reject fewer than one trip per slot`() {
    assertThrows<IllegalArgumentException> { validPolicy().copy(maxTripsPerSlot = 0) }
  }

  @Test
  fun `should reject a non-positive trip distance budget`() {
    assertThrows<IllegalArgumentException> { validPolicy().copy(maxTripKmBudget = 0.0) }
  }

  @Test
  fun `should reject a non-finite trip distance budget`() {
    assertThrows<IllegalArgumentException> { validPolicy().copy(maxTripKmBudget = Double.NaN) }
    assertThrows<IllegalArgumentException> {
      validPolicy().copy(maxTripKmBudget = Double.POSITIVE_INFINITY)
    }
  }

  @Test
  fun `should reject a negative or non-finite detour threshold`() {
    assertThrows<IllegalArgumentException> { validPolicy().copy(maxDetourKm = -0.1) }
    assertThrows<IllegalArgumentException> { validPolicy().copy(maxDetourKm = Double.NaN) }
  }
}
