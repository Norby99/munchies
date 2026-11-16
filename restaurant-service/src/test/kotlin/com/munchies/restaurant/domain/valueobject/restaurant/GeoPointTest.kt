package com.munchies.restaurant.domain.valueobject.restaurant

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GeoPointTest {

  @Test
  fun `should accept Riccione`() {
    val point = GeoPoint(lat = 44.0, lng = 12.6522)
    assertEquals(44.0, point.lat)
  }

  @Test
  fun `should reject latitude outside the valid range`() {
    assertThrows<IllegalArgumentException> { GeoPoint(lat = 90.5, lng = 0.0) }
  }

  @Test
  fun `should reject longitude outside the valid range`() {
    assertThrows<IllegalArgumentException> { GeoPoint(lat = 0.0, lng = -180.5) }
  }

  @Test
  fun `should reject non-finite coordinates`() {
    assertThrows<IllegalArgumentException> { GeoPoint(lat = Double.NaN, lng = 0.0) }
  }
}
