package com.munchies.restaurant.domain.valueobject.menu

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class VariationOptionNameTest {
  @Test
  fun `should create variation option name successfully`() {
    val name = VariationOptionName.of("Large")
    assertEquals("Large", name.value)
  }

  @Test
  fun `should trim variation option name`() {
    val name = VariationOptionName.of("  Gluten Free  ")
    assertEquals("Gluten Free", name.value)
  }

  @Test
  fun `should throw when variation option name is blank`() {
    assertThrows<IllegalArgumentException> { VariationOptionName.of("") }
    assertThrows<IllegalArgumentException> { VariationOptionName.of("   ") }
  }

  @Test
  fun `should throw when variation option name exceeds 100 characters`() {
    val longName = "A".repeat(101)
    assertThrows<IllegalArgumentException> { VariationOptionName.of(longName) }
  }
}
