package com.munchies.restaurant.domain.valueobject.menu

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MenuItemDescriptionTest {
  @Test
  fun `should create description successfully`() {
    val desc = MenuItemDescription.of("Delicious cheese pizza")
    assertEquals("Delicious cheese pizza", desc.value)
  }

  @Test
  fun `should trim description`() {
    val desc = MenuItemDescription.of("  Tasty  ")
    assertEquals("Tasty", desc.value)
  }

  @Test
  fun `should throw when description is blank`() {
    assertThrows<IllegalArgumentException> { MenuItemDescription.of("") }
    assertThrows<IllegalArgumentException> { MenuItemDescription.of("   ") }
  }

  @Test
  fun `should throw when description exceeds 500 characters`() {
    val longDesc = "A".repeat(501)
    assertThrows<IllegalArgumentException> { MenuItemDescription.of(longDesc) }
  }
}
