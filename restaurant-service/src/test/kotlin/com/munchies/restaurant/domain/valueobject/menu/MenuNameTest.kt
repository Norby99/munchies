package com.munchies.restaurant.domain.valueobject.menu

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MenuNameTest {
  @Test
  fun `should create menu name successfully`() {
    val name = MenuName.of("Dinner")
    assertEquals("Dinner", name.value)
  }

  @Test
  fun `should trim menu name`() {
    val name = MenuName.of("  Dinner Menu  ")
    assertEquals("Dinner Menu", name.value)
  }

  @Test
  fun `should throw when menu name is blank`() {
    assertThrows<IllegalArgumentException> { MenuName.of("") }
    assertThrows<IllegalArgumentException> { MenuName.of("   ") }
  }

  @Test
  fun `should throw when menu name exceeds 50 characters`() {
    val longName = "A".repeat(51)
    assertThrows<IllegalArgumentException> { MenuName.of(longName) }
  }
}
