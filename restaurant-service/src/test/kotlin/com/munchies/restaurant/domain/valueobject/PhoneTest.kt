package com.munchies.restaurant.domain.valueobject

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@DisplayName("Phone value objects")
class PhoneTest {

  @Test
  @DisplayName("should create phone with valid digits only")
  fun shouldCreatePhoneWithValidDigitsOnly() {
    val phone = Phone.of("1234567890")

    assertEquals("1234567890", phone.value)
  }

  @Test
  @DisplayName("should create phone with plus prefix")
  fun shouldCreatePhoneWithPlusPrefix() {
    val phone = Phone.of("+1-234-567-8900")

    assertEquals("+1-234-567-8900", phone.value)
  }

  @Test
  @DisplayName("should create phone with parentheses and dashes")
  fun shouldCreatePhoneWithParenthesesAndDashes() {
    val phone = Phone.of("(555) 123-4567")

    assertEquals("(555) 123-4567", phone.value)
  }

  @Test
  @DisplayName("should create phone with spaces")
  fun shouldCreatePhoneWithSpaces() {
    val phone = Phone.of("555 123 4567")

    assertEquals("555 123 4567", phone.value)
  }

  @Test
  @DisplayName("should trim whitespace from phone number")
  fun shouldTrimWhitespaceFromPhoneNumber() {
    val phone = Phone.of("  9876543210  ")

    assertEquals("9876543210", phone.value)
  }

  @Test
  @DisplayName("should throw exception when phone is blank")
  fun shouldThrowExceptionWhenPhoneIsBlank() {
    assertThrows<IllegalArgumentException> {
      Phone.of("")
    }
  }

  @Test
  @DisplayName("should throw exception when phone is only whitespace")
  fun shouldThrowExceptionWhenPhoneIsOnlyWhitespace() {
    assertThrows<IllegalArgumentException> {
      Phone.of("   ")
    }
  }

  @Test
  @DisplayName("should throw exception when phone contains invalid characters")
  fun shouldThrowExceptionWhenPhoneContainsInvalidCharacters() {
    assertThrows<IllegalArgumentException> {
      Phone.of("123-456-7890 ext")
    }
  }

  @Test
  @DisplayName("should throw exception when phone contains letters")
  fun shouldThrowExceptionWhenPhoneContainsLetters() {
    assertThrows<IllegalArgumentException> {
      Phone.of("555-ABC-4567")
    }
  }

  @Test
  @DisplayName("should throw exception when phone exceeds maximum length")
  fun shouldThrowExceptionWhenPhoneExceedsMaximumLength() {
    val tooLongPhone = "1".repeat(21)
    assertThrows<IllegalArgumentException> {
      Phone.of(tooLongPhone)
    }
  }

  @Test
  @DisplayName("should accept phone at maximum allowed length")
  fun shouldAcceptPhoneAtMaximumAllowedLength() {
    val maxLengthPhone = "1".repeat(20)

    val phone = Phone.of(maxLengthPhone)

    assertEquals(20, phone.value.length)
  }

  @Test
  @DisplayName("should create phone with plus and digits")
  fun shouldCreatePhoneWithPlusAndDigits() {
    val phone = Phone.of("+33123456789")

    assertEquals("+33123456789", phone.value)
  }

  @Test
  @DisplayName("should create phone with dashes only between digits")
  fun shouldCreatePhoneWithDashesOnlyBetweenDigits() {
    val phone = Phone.of("1-234-567-8901")

    assertEquals("1-234-567-8901", phone.value)
  }

  @Test
  @DisplayName("should create two phones with same value equal")
  fun shouldCreateTwoPhoneWithSameValueEqual() {
    val phone1 = Phone.of("5551234567")
    val phone2 = Phone.of("5551234567")

    assertEquals(phone1, phone2)
  }

  @Test
  @DisplayName("should handle international format phone")
  fun shouldHandleInternationalFormatPhone() {
    val phone = Phone.of("+44 20 7946 0958")

    assertEquals("+44 20 7946 0958", phone.value)
  }
}
