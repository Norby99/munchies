package com.munchies.restaurant.domain.valueobject.restaurant

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class EmailTest {

  @Test
  fun `should create email with valid format`() {
    val email = Email.of("user@example.com")

    assertEquals("user@example.com", email.value)
  }

  @Test
  fun `should normalize email to lowercase`() {
    val email = Email.of("User@Example.COM")

    assertEquals("user@example.com", email.value)
  }

  @Test
  fun `should trim whitespace from email`() {
    val email = Email.of("  admin@company.com  ")

    assertEquals("admin@company.com", email.value)
  }

  @Test
  fun `should create email with plus addressing`() {
    val email = Email.of("user+tag@example.com")

    assertEquals("user+tag@example.com", email.value)
  }

  @Test
  fun `should create email with dots in local part`() {
    val email = Email.of("first.last@example.com")

    assertEquals("first.last@example.com", email.value)
  }

  @Test
  fun `should create email with hyphens in domain`() {
    val email = Email.of("user@my-example.com")

    assertEquals("user@my-example.com", email.value)
  }

  @Test
  fun `should create email with numbers in local part`() {
    val email = Email.of("user123@example.com")

    assertEquals("user123@example.com", email.value)
  }

  @Test
  fun `should create email with subdomain`() {
    val email = Email.of("user@mail.example.com")

    assertEquals("user@mail.example.com", email.value)
  }

  @Test
  fun `should throw exception when email is blank`() {
    assertThrows<IllegalArgumentException> {
      Email.of("")
    }
  }

  @Test
  fun `should throw exception when email has no at sign`() {
    assertThrows<IllegalArgumentException> {
      Email.of("userexample.com")
    }
  }

  @Test
  fun `should throw exception when email has no local part`() {
    assertThrows<IllegalArgumentException> {
      Email.of("@example.com")
    }
  }

  @Test
  fun `should throw exception when email has no domain`() {
    assertThrows<IllegalArgumentException> {
      Email.of("user@")
    }
  }

  @Test
  fun `should throw exception when email has no TLD`() {
    assertThrows<IllegalArgumentException> {
      Email.of("user@example")
    }
  }

  @Test
  fun `should throw exception when email has invalid characters`() {
    assertThrows<IllegalArgumentException> {
      Email.of("user name@example.com")
    }
  }

  @Test
  fun `should throw exception when email exceeds maximum length`() {
    val tooLongEmail = "a".repeat(244) + "@example.com"

    assertThrows<IllegalArgumentException> {
      Email.of(tooLongEmail)
    }
  }

  @Test
  fun `should accept email at maximum allowed length`() {
    val maxLengthEmail = "a".repeat(243) + "@example.com"

    val email = Email.of(maxLengthEmail)

    assertEquals(255, email.value.length)
  }

  @Test
  fun `should create two emails with same value equal`() {
    val email1 = Email.of("test@example.com")
    val email2 = Email.of("test@example.com")

    assertEquals(email1, email2)
  }

  @Test
  fun `should create two emails with different case equal after normalization`() {
    val email1 = Email.of("User@Example.COM")
    val email2 = Email.of("user@example.com")

    assertEquals(email1, email2)
  }
}
