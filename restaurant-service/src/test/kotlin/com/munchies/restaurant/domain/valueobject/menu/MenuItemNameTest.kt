package com.munchies.restaurant.domain.valueobject.menu

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MenuItemNameTest {
  @Test
  fun `should create menu item name successfully`() {
    val name = MenuItemName.of("Margherita")
    assertEquals("Margherita", name.value)
  }

  @Test
  fun `should trim menu item name`() {
    val name = MenuItemName.of("  Margherita  ")
    assertEquals("Margherita", name.value)
  }

  @Test
  fun `should throw when menu item name is blank`() {
    assertThrows<IllegalArgumentException> { MenuItemName.of("") }
    assertThrows<IllegalArgumentException> { MenuItemName.of("   ") }
  }

  @Test
  fun `should throw when menu item name exceeds 150 characters`() {
    val longName = "A".repeat(151)
    assertThrows<IllegalArgumentException> { MenuItemName.of(longName) }
  }
}
