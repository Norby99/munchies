package com.munchies.restaurant.domain.valueobject

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@DisplayName("Email value objects")
class EmailTest {

  @Test
  @DisplayName("should create email with valid format")
  fun shouldCreateEmailWithValidFormat() {
    val email = Email.of("user@example.com")

    assertEquals("user@example.com", email.value)
  }

  @Test
  @DisplayName("should normalize email to lowercase")
  fun shouldNormalizeEmailToLowercase() {
    val email = Email.of("User@Example.COM")

    assertEquals("user@example.com", email.value)
  }

  @Test
  @DisplayName("should trim whitespace from email")
  fun shouldTrimWhitespaceFromEmail() {
    val email = Email.of("  admin@company.com  ")

    assertEquals("admin@company.com", email.value)
  }

  @Test
  @DisplayName("should create email with plus addressing")
  fun shouldCreateEmailWithPlusAddressing() {
    val email = Email.of("user+tag@example.com")

    assertEquals("user+tag@example.com", email.value)
  }

  @Test
  @DisplayName("should create email with dots in local part")
  fun shouldCreateEmailWithDotsInLocalPart() {
    val email = Email.of("first.last@example.com")

    assertEquals("first.last@example.com", email.value)
  }

  @Test
  @DisplayName("should create email with hyphens in domain")
  fun shouldCreateEmailWithHyphensInDomain() {
    val email = Email.of("user@my-example.com")

    assertEquals("user@my-example.com", email.value)
  }

  @Test
  @DisplayName("should create email with numbers in local part")
  fun shouldCreateEmailWithNumbersInLocalPart() {
    val email = Email.of("user123@example.com")

    assertEquals("user123@example.com", email.value)
  }

  @Test
  @DisplayName("should create email with subdomain")
  fun shouldCreateEmailWithSubdomain() {
    val email = Email.of("user@mail.example.com")

    assertEquals("user@mail.example.com", email.value)
  }

  @Test
  @DisplayName("should throw exception when email is blank")
  fun shouldThrowExceptionWhenEmailIsBlank() {
    assertThrows<IllegalArgumentException> {
      Email.of("")
    }
  }

  @Test
  @DisplayName("should throw exception when email has no at sign")
  fun shouldThrowExceptionWhenEmailHasNoAtSign() {
    assertThrows<IllegalArgumentException> {
      Email.of("userexample.com")
    }
  }

  @Test
  @DisplayName("should throw exception when email has no local part")
  fun shouldThrowExceptionWhenEmailHasNoLocalPart() {
    assertThrows<IllegalArgumentException> {
      Email.of("@example.com")
    }
  }

  @Test
  @DisplayName("should throw exception when email has no domain")
  fun shouldThrowExceptionWhenEmailHasNoDomain() {
    assertThrows<IllegalArgumentException> {
      Email.of("user@")
    }
  }

  @Test
  @DisplayName("should throw exception when email has no TLD")
  fun shouldThrowExceptionWhenEmailHasNoTLD() {
    assertThrows<IllegalArgumentException> {
      Email.of("user@example")
    }
  }

  @Test
  @DisplayName("should throw exception when email has invalid characters")
  fun shouldThrowExceptionWhenEmailHasInvalidCharacters() {
    assertThrows<IllegalArgumentException> {
      Email.of("user name@example.com")
    }
  }

  @Test
  @DisplayName("should throw exception when email exceeds maximum length")
  fun shouldThrowExceptionWhenEmailExceedsMaximumLength() {
    val tooLongEmail = "a".repeat(244) + "@example.com"

    assertThrows<IllegalArgumentException> {
      Email.of(tooLongEmail)
    }
  }

  @Test
  @DisplayName("should accept email at maximum allowed length")
  fun shouldAcceptEmailAtMaximumAllowedLength() {
    val maxLengthEmail = "a".repeat(243) + "@example.com"

    val email = Email.of(maxLengthEmail)

    assertEquals(255, email.value.length)
  }

  @Test
  @DisplayName("should create two emails with same value equal")
  fun shouldCreateTwoEmailsWithSameValueEqual() {
    val email1 = Email.of("test@example.com")
    val email2 = Email.of("test@example.com")

    assertEquals(email1, email2)
  }

  @Test
  @DisplayName("should create two emails with different case equal after normalization")
  fun shouldCreateTwoEmailsWithDifferentCaseEqualAfterNormalization() {
    val email1 = Email.of("User@Example.COM")
    val email2 = Email.of("user@example.com")

    assertEquals(email1, email2)
  }
}
