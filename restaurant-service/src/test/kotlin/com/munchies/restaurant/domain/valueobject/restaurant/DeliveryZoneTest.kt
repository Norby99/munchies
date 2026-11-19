package com.munchies.restaurant.domain.valueobject.restaurant

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class DeliveryZoneTest {

  private val triangle = listOf(
    GeoPoint(44.00, 12.60),
    GeoPoint(44.00, 12.70),
    GeoPoint(44.05, 12.65),
  )

  @Test
  fun `should accept a triangle`() {
    val zone = DeliveryZone(id = "z1", name = "Centro", boundary = triangle)
    assertEquals(3, zone.boundary.size)
  }

  @Test
  fun `should reject a blank name`() {
    assertThrows<IllegalArgumentException> { DeliveryZone("z1", "  ", triangle) }
  }

  @Test
  fun `should reject a blank id`() {
    assertThrows<IllegalArgumentException> { DeliveryZone("", "Centro", triangle) }
  }

  @Test
  fun `should reject a name longer than the limit`() {
    val name = "x".repeat(DeliveryZone.MAX_NAME_LENGTH + 1)
    assertThrows<IllegalArgumentException> { DeliveryZone("z1", name, triangle) }
  }

  @Test
  fun `should reject a boundary with fewer than three distinct vertices`() {
    val degenerate = listOf(GeoPoint(44.0, 12.6), GeoPoint(44.1, 12.6), GeoPoint(44.0, 12.6))
    assertThrows<IllegalArgumentException> { DeliveryZone("z1", "Centro", degenerate) }
  }

  @Test
  fun `should reject a boundary with too many vertices`() {
    val tooMany = (0..DeliveryZone.MAX_VERTICES).map { GeoPoint(44.0, 12.0 + it * 0.0001) }
    assertThrows<IllegalArgumentException> { DeliveryZone("z1", "Centro", tooMany) }
  }
}
