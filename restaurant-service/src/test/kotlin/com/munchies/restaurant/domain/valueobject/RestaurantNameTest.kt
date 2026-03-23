package com.munchies.restaurant.domain.valueobject

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@DisplayName("Restaurant name value objects")
class RestaurantNameTest {

  @Test
  @DisplayName("should create restaurant name with valid data")
  fun shouldCreateRestaurantNameWithValidData() {
    val name = RestaurantName.of("The Italian Kitchen")

    assertEquals("The Italian Kitchen", name.value)
  }

  @Test
  @DisplayName("should trim whitespace from restaurant name")
  fun shouldTrimWhitespaceFromRestaurantName() {
    val name = RestaurantName.of("  Burger Palace  ")

    assertEquals("Burger Palace", name.value)
  }

  @Test
  @DisplayName("should create restaurant name with single word")
  fun shouldCreateRestaurantNameWithSingleWord() {
    val name = RestaurantName.of("Sushi")

    assertEquals("Sushi", name.value)
  }

  @Test
  @DisplayName("should create restaurant name with numbers")
  fun shouldCreateRestaurantNameWithNumbers() {
    val name = RestaurantName.of("99 Bottles Wine Bar")

    assertEquals("99 Bottles Wine Bar", name.value)
  }

  @Test
  @DisplayName("should create restaurant name with special characters")
  fun shouldCreateRestaurantNameWithSpecialCharacters() {
    val name = RestaurantName.of("Tom's Deli & Café")

    assertEquals("Tom's Deli & Café", name.value)
  }

  @Test
  @DisplayName("should throw exception when name is blank")
  fun shouldThrowExceptionWhenNameIsBlank() {
    assertThrows<IllegalArgumentException> {
      RestaurantName.of("")
    }
  }

  @Test
  @DisplayName("should throw exception when name is only whitespace")
  fun shouldThrowExceptionWhenNameIsOnlyWhitespace() {
    assertThrows<IllegalArgumentException> {
      RestaurantName.of("   ")
    }
  }

  @Test
  @DisplayName("should throw exception when name exceeds maximum length")
  fun shouldThrowExceptionWhenNameExceedsMaximumLength() {
    val tooLongName = "a".repeat(256)

    assertThrows<IllegalArgumentException> {
      RestaurantName.of(tooLongName)
    }
  }

  @Test
  @DisplayName("should accept name at maximum allowed length")
  fun shouldAcceptNameAtMaximumAllowedLength() {
    val maxLengthName = "a".repeat(255)

    val name = RestaurantName.of(maxLengthName)

    assertEquals(255, name.value.length)
  }

  @Test
  @DisplayName("should create restaurant name with multiple spaces between words")
  fun shouldCreateRestaurantNameWithMultipleSpacesBetweenWords() {
    val name = RestaurantName.of("  The  Grand   Café  ")

    assertEquals("The  Grand   Café", name.value)
  }

  @Test
  @DisplayName("should create two restaurant names with same value equal")
  fun shouldCreateTwoRestaurantNamesWithSameValueEqual() {
    val name1 = RestaurantName.of("Pizza House")
    val name2 = RestaurantName.of("Pizza House")

    assertEquals(name1, name2)
  }

  @Test
  @DisplayName("should preserve original case after creation")
  fun shouldPreserveOriginalCaseAfterCreation() {
    val name = RestaurantName.of("TeRrAcE RestAurAnt")

    assertEquals("TeRrAcE RestAurAnt", name.value)
  }

  @Test
  @DisplayName("should handle unicode characters in name")
  fun shouldHandleUnicodeCharactersInName() {
    val name = RestaurantName.of("日本料理 Japanese Kitchen")

    assertEquals("日本料理 Japanese Kitchen", name.value)
  }

  @Test
  @DisplayName("should create restaurant name with hyphens")
  fun shouldCreateRestaurantNameWithHyphens() {
    val name = RestaurantName.of("The Red Lion")

    assertEquals("The Red Lion", name.value)
  }
}
