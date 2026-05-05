package com.munchies.restaurant.domain.valueobject.menu

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CategoryNameTest {
  @Test
  fun `should create category name successfully`() {
    val name = CategoryName.of("Pizza")
    assertEquals("Pizza", name.value)
  }

  @Test
  fun `should trim category name`() {
    val name = CategoryName.of("  Desserts  ")
    assertEquals("Desserts", name.value)
  }

  @Test
  fun `should throw when category name is blank`() {
    assertThrows<IllegalArgumentException> { CategoryName.of("") }
    assertThrows<IllegalArgumentException> { CategoryName.of("   ") }
  }

  @Test
  fun `should throw when category name exceeds 100 characters`() {
    val longName = "A".repeat(101)
    assertThrows<IllegalArgumentException> { CategoryName.of(longName) }
  }
}
