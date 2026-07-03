package com.munchies.restaurant.domain.valueobject.menu

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class VariationNameTest {
  @Test
  fun `should create variation name successfully`() {
    val name = VariationName("Size")
    assertEquals("Size", name.value)
  }

  @Test
  fun `should trim variation name`() {
    val name = VariationName("  Crust Type  ")
    assertEquals("Crust Type", name.value)
  }

  @Test
  fun `should throw when variation name is blank`() {
    assertThrows<IllegalArgumentException> { VariationName("") }
    assertThrows<IllegalArgumentException> { VariationName("   ") }
  }

  @Test
  fun `should throw when variation name exceeds 100 characters`() {
    val longName = "A".repeat(101)
    assertThrows<IllegalArgumentException> { VariationName(longName) }
  }
}
