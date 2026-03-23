package com.munchies.restaurant.domain.valueobject

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@DisplayName("Restaurant id value objects")
class RestaurantIdTest {

  @Test
  @DisplayName("should create restaurant id with factory method")
  fun shouldCreateRestaurantIdWithFactoryMethod() {
    val id = RestaurantId.of("test-restaurant-id-123")

    assertEquals("test-restaurant-id-123", id.value)
  }

  @Test
  @DisplayName("should generate unique id when created without parameter")
  fun shouldGenerateUniqueIdWhenCreatedWithoutParameter() {
    val id1 = RestaurantId()
    val id2 = RestaurantId()

    assertNotEquals(id1.value, id2.value)
  }

  @Test
  @DisplayName("should throw exception when id is blank")
  fun shouldThrowExceptionWhenIdIsBlank() {
    assertThrows<IllegalArgumentException> {
      RestaurantId.of("")
    }
  }

  @Test
  @DisplayName("should throw exception when id is only whitespace")
  fun shouldThrowExceptionWhenIdIsOnlyWhitespace() {
    assertThrows<IllegalArgumentException> {
      RestaurantId.of("   ")
    }
  }

  @Test
  @DisplayName("should preserve id value after creation")
  fun shouldPreserveIdValueAfterCreation() {
    val originalId = "restaurant-12345"
    val id = RestaurantId.of(originalId)

    assertEquals(originalId, id.value)
  }

  @Test
  @DisplayName("should create two ids with same value equal")
  fun shouldCreateTwoIdsWithSameValueEqual() {
    val id1 = RestaurantId.of("same-id")
    val id2 = RestaurantId.of("same-id")

    assertEquals(id1, id2)
  }

  @Test
  @DisplayName("should create ids with UUID format")
  fun shouldCreateIdsWithUUIDFormat() {
    val id1 = RestaurantId()
    val id2 = RestaurantId()

    assertEquals(36, id1.value.length)
    assertEquals(36, id2.value.length)
  }
}
