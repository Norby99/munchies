package com.munchies.restaurant.domain.valueobject.restaurant

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AddressTest {

  @Test
  fun `should create address with valid data using factory method`() {
    val address = Address.of("123 Main Street")

    assertEquals("123 Main Street", address.value)
  }

  @Test
  fun `should trim whitespace from address`() {
    val address = Address.of("  456 Oak Avenue  ")

    assertEquals("456 Oak Avenue", address.value)
  }

  @Test
  fun `should throw exception when address is blank`() {
    assertThrows<IllegalArgumentException> {
      Address.of("")
    }
  }

  @Test
  fun `should throw exception when address is only whitespace`() {
    assertThrows<IllegalArgumentException> {
      Address.of("   ")
    }
  }

  @Test
  fun `should throw exception when address exceeds maximum length`() {
    val tooLongAddress = "a".repeat(501)

    assertThrows<IllegalArgumentException> {
      Address.of(tooLongAddress)
    }
  }

  @Test
  fun `should accept address at maximum allowed length`() {
    val maxLengthAddress = "a".repeat(500)

    val address = Address.of(maxLengthAddress)

    assertEquals(500, address.value.length)
  }

  @Test
  fun `should create address with special characters`() {
    val address = Address.of("123 Main St. #5, Apt. B")

    assertEquals("123 Main St. #5, Apt. B", address.value)
  }

  @Test
  fun `should create address with numbers and letters`() {
    val address = Address.of("789 Elm Street")

    assertEquals("789 Elm Street", address.value)
  }

  @Test
  fun `should create address with complex format`() {
    val address = Address.of("1000 Park Avenue, Suite 200, Building A")

    assertEquals("1000 Park Avenue, Suite 200, Building A", address.value)
  }

  @Test
  fun `should preserve address format after creation`() {
    val originalAddress = "100 Broadway, New York, NY 10001"
    val address = Address.of(originalAddress)

    assertEquals(originalAddress, address.value)
  }

  @Test
  fun `should create two addresses with same value equal`() {
    val address1 = Address.of("200 Madison Avenue")
    val address2 = Address.of("200 Madison Avenue")

    assertEquals(address1, address2)
  }

  @Test
  fun `should handle address with leading and trailing tabs`() {
    val address = Address.of("\t999 Fifth Avenue\t")

    assertEquals("999 Fifth Avenue", address.value)
  }

  @Test
  fun `should handle address with mixed whitespace`() {
    val address = Address.of("  \t  555 Park Place  \n  ")

    assertEquals("555 Park Place", address.value)
  }
}
