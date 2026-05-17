package com.munchies.restaurant.domain.valueobject

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RestaurantIdTest {

  @Test
  fun `should create restaurant id with factory method`() {
    val id = RestaurantId.of("test-restaurant-id-123")

    assertEquals("test-restaurant-id-123", id.value)
  }

  @Test
  fun `should generate unique id when created without parameter`() {
    val id1 = RestaurantId()
    val id2 = RestaurantId()

    assertNotEquals(id1.value, id2.value)
  }

  @Test
  fun `should throw exception when id is blank`() {
    assertThrows<IllegalArgumentException> {
      RestaurantId.of("")
    }
  }

  @Test
  fun `should throw exception when id is only whitespace`() {
    assertThrows<IllegalArgumentException> {
      RestaurantId.of("   ")
    }
  }

  @Test
  fun `should preserve id value after creation`() {
    val originalId = "restaurant-12345"
    val id = RestaurantId.of(originalId)

    assertEquals(originalId, id.value)
  }

  @Test
  fun `should create two ids with same value equal`() {
    val id1 = RestaurantId.of("same-id")
    val id2 = RestaurantId.of("same-id")

    assertEquals(id1, id2)
  }

  @Test
  fun `should create ids with UUID format`() {
    val id1 = RestaurantId()
    val id2 = RestaurantId()

    assertEquals(36, id1.value.length)
    assertEquals(36, id2.value.length)
  }
}
