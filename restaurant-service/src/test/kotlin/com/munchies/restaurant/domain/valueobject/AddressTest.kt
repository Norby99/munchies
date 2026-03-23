package com.munchies.restaurant.domain.valueobject

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@DisplayName("Address value objects")
class AddressTest {

  @Test
  @DisplayName("should create address with valid data using factory method")
  fun shouldCreateAddressWithValidData() {
    val address = Address.of("123 Main Street")

    assertEquals("123 Main Street", address.value)
  }

  @Test
  @DisplayName("should trim whitespace from address")
  fun shouldTrimWhitespaceFromAddress() {
    val address = Address.of("  456 Oak Avenue  ")

    assertEquals("456 Oak Avenue", address.value)
  }

  @Test
  @DisplayName("should throw exception when address is blank")
  fun shouldThrowExceptionWhenAddressIsBlank() {
    assertThrows<IllegalArgumentException> {
      Address.of("")
    }
  }

  @Test
  @DisplayName("should throw exception when address is only whitespace")
  fun shouldThrowExceptionWhenAddressIsOnlyWhitespace() {
    assertThrows<IllegalArgumentException> {
      Address.of("   ")
    }
  }

  @Test
  @DisplayName("should throw exception when address exceeds maximum length")
  fun shouldThrowExceptionWhenAddressExceedsMaximumLength() {
    val tooLongAddress = "a".repeat(501)

    assertThrows<IllegalArgumentException> {
      Address.of(tooLongAddress)
    }
  }

  @Test
  @DisplayName("should accept address at maximum allowed length")
  fun shouldAcceptAddressAtMaximumAllowedLength() {
    val maxLengthAddress = "a".repeat(500)

    val address = Address.of(maxLengthAddress)

    assertEquals(500, address.value.length)
  }

  @Test
  @DisplayName("should create address with special characters")
  fun shouldCreateAddressWithSpecialCharacters() {
    val address = Address.of("123 Main St. #5, Apt. B")

    assertEquals("123 Main St. #5, Apt. B", address.value)
  }

  @Test
  @DisplayName("should create address with numbers and letters")
  fun shouldCreateAddressWithNumbersAndLetters() {
    val address = Address.of("789 Elm Street")

    assertEquals("789 Elm Street", address.value)
  }

  @Test
  @DisplayName("should create address with complex format")
  fun shouldCreateAddressWithComplexFormat() {
    val address = Address.of("1000 Park Avenue, Suite 200, Building A")

    assertEquals("1000 Park Avenue, Suite 200, Building A", address.value)
  }

  @Test
  @DisplayName("should preserve address format after creation")
  fun shouldPreserveAddressFormatAfterCreation() {
    val originalAddress = "100 Broadway, New York, NY 10001"
    val address = Address.of(originalAddress)

    assertEquals(originalAddress, address.value)
  }

  @Test
  @DisplayName("should create two addresses with same value equal")
  fun shouldCreateTwoAddressesWithSameValueEqual() {
    val address1 = Address.of("200 Madison Avenue")
    val address2 = Address.of("200 Madison Avenue")

    assertEquals(address1, address2)
  }

  @Test
  @DisplayName("should handle address with leading and trailing tabs")
  fun shouldHandleAddressWithLeadingAndTrailingTabs() {
    val address = Address.of("\t999 Fifth Avenue\t")

    assertEquals("999 Fifth Avenue", address.value)
  }

  @Test
  @DisplayName("should handle address with mixed whitespace")
  fun shouldHandleAddressWithMixedWhitespace() {
    val address = Address.of("  \t  555 Park Place  \n  ")

    assertEquals("555 Park Place", address.value)
  }
}
