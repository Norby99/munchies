package com.munchies.restaurant.domain.valueobject.restaurant

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RestaurantNameTest {

  @Test
  fun `should create restaurant name with valid data`() {
    val name = RestaurantName.of("The Italian Kitchen")

    assertEquals("The Italian Kitchen", name.value)
  }

  @Test
  fun `should trim whitespace from restaurant name`() {
    val name = RestaurantName.of("  Burger Palace  ")

    assertEquals("Burger Palace", name.value)
  }

  @Test
  fun `should create restaurant name with single word`() {
    val name = RestaurantName.of("Sushi")

    assertEquals("Sushi", name.value)
  }

  @Test
  fun `should create restaurant name with numbers`() {
    val name = RestaurantName.of("99 Bottles Wine Bar")

    assertEquals("99 Bottles Wine Bar", name.value)
  }

  @Test
  fun `should create restaurant name with special characters`() {
    val name = RestaurantName.of("Tom's Deli & Café")

    assertEquals("Tom's Deli & Café", name.value)
  }

  @Test
  fun `should throw exception when name is blank`() {
    assertThrows<IllegalArgumentException> {
      RestaurantName.of("")
    }
  }

  @Test
  fun `should throw exception when name is only whitespace`() {
    assertThrows<IllegalArgumentException> {
      RestaurantName.of("   ")
    }
  }

  @Test
  fun `should throw exception when name exceeds maximum length`() {
    val tooLongName = "a".repeat(256)

    assertThrows<IllegalArgumentException> {
      RestaurantName.of(tooLongName)
    }
  }

  @Test
  fun `should accept name at maximum allowed length`() {
    val maxLengthName = "a".repeat(255)

    val name = RestaurantName.of(maxLengthName)

    assertEquals(255, name.value.length)
  }

  @Test
  fun `should create restaurant name with multiple spaces between words`() {
    val name = RestaurantName.of("  The  Grand   Café  ")

    assertEquals("The  Grand   Café", name.value)
  }

  @Test
  fun `should create two restaurant names with same value equal`() {
    val name1 = RestaurantName.of("Pizza House")
    val name2 = RestaurantName.of("Pizza House")

    assertEquals(name1, name2)
  }

  @Test
  fun `should preserve original case after creation`() {
    val name = RestaurantName.of("TeRrAcE RestAurAnt")

    assertEquals("TeRrAcE RestAurAnt", name.value)
  }

  @Test
  fun `should handle unicode characters in name`() {
    val name = RestaurantName.of("日本料理 Japanese Kitchen")

    assertEquals("日本料理 Japanese Kitchen", name.value)
  }

  @Test
  fun `should create restaurant name with hyphens`() {
    val name = RestaurantName.of("The Red Lion")

    assertEquals("The Red Lion", name.value)
  }
}
