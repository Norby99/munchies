package com.munchies.restaurant.domain.valueobject.restaurant

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PhoneTest {

  @Test
  fun `should create phone with valid digits only`() {
    val phone = Phone.of("1234567890")

    assertEquals("1234567890", phone.value)
  }

  @Test
  fun `should create phone with plus prefix`() {
    val phone = Phone.of("+1-234-567-8900")

    assertEquals("+1-234-567-8900", phone.value)
  }

  @Test
  fun `should create phone with parentheses and dashes`() {
    val phone = Phone.of("(555) 123-4567")

    assertEquals("(555) 123-4567", phone.value)
  }

  @Test
  fun `should create phone with spaces`() {
    val phone = Phone.of("555 123 4567")

    assertEquals("555 123 4567", phone.value)
  }

  @Test
  fun `should trim whitespace from phone number`() {
    val phone = Phone.of("  9876543210  ")

    assertEquals("9876543210", phone.value)
  }

  @Test
  fun `should throw exception when phone is blank`() {
    assertThrows<IllegalArgumentException> {
      Phone.of("")
    }
  }

  @Test
  fun `should throw exception when phone is only whitespace`() {
    assertThrows<IllegalArgumentException> {
      Phone.of("   ")
    }
  }

  @Test
  fun `should throw exception when phone contains invalid characters`() {
    assertThrows<IllegalArgumentException> {
      Phone.of("123-456-7890 ext")
    }
  }

  @Test
  fun `should throw exception when phone contains letters`() {
    assertThrows<IllegalArgumentException> {
      Phone.of("555-ABC-4567")
    }
  }

  @Test
  fun `should throw exception when phone exceeds maximum length`() {
    val tooLongPhone = "1".repeat(21)
    assertThrows<IllegalArgumentException> {
      Phone.of(tooLongPhone)
    }
  }

  @Test
  fun `should accept phone at maximum allowed length`() {
    val maxLengthPhone = "1".repeat(20)

    val phone = Phone.of(maxLengthPhone)

    assertEquals(20, phone.value.length)
  }

  @Test
  fun `should create phone with plus and digits`() {
    val phone = Phone.of("+33123456789")

    assertEquals("+33123456789", phone.value)
  }

  @Test
  fun `should create phone with dashes only between digits`() {
    val phone = Phone.of("1-234-567-8901")

    assertEquals("1-234-567-8901", phone.value)
  }

  @Test
  fun `should create two phones with same value equal`() {
    val phone1 = Phone.of("5551234567")
    val phone2 = Phone.of("5551234567")

    assertEquals(phone1, phone2)
  }

  @Test
  fun `should handle international format phone`() {
    val phone = Phone.of("+44 20 7946 0958")

    assertEquals("+44 20 7946 0958", phone.value)
  }
}
