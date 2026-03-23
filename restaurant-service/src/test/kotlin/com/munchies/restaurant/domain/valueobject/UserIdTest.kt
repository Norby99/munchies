package com.munchies.restaurant.domain.valueobject

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@DisplayName("Used id value objects")
class UserIdTest {

  @Test
  @DisplayName("should create user id with factory method")
  fun shouldCreateUserIdWithFactoryMethod() {
    val id = UserId.of("test-user-id-456")

    assertEquals("test-user-id-456", id.value)
  }

  @Test
  @DisplayName("should generate unique id when created without parameter")
  fun shouldGenerateUniqueIdWhenCreatedWithoutParameter() {
    val id1 = UserId()
    val id2 = UserId()

    assertNotEquals(id1.value, id2.value)
  }

  @Test
  @DisplayName("should throw exception when id is blank")
  fun shouldThrowExceptionWhenIdIsBlank() {
    assertThrows<IllegalArgumentException> {
      UserId.of("")
    }
  }

  @Test
  @DisplayName("should throw exception when id is only whitespace")
  fun shouldThrowExceptionWhenIdIsOnlyWhitespace() {
    assertThrows<IllegalArgumentException> {
      UserId.of("   ")
    }
  }

  @Test
  @DisplayName("should preserve id value after creation")
  fun shouldPreserveIdValueAfterCreation() {
    val originalId = "user-67890"
    val id = UserId.of(originalId)

    assertEquals(originalId, id.value)
  }

  @Test
  @DisplayName("should create two ids with same value equal")
  fun shouldCreateTwoIdsWithSameValueEqual() {
    val id1 = UserId.of("same-user-id")
    val id2 = UserId.of("same-user-id")

    assertEquals(id1, id2)
  }

  @Test
  @DisplayName("should create ids with UUID format")
  fun shouldCreateIdsWithUUIDFormat() {
    val id1 = UserId()
    val id2 = UserId()

    assertEquals(36, id1.value.length)
    assertEquals(36, id2.value.length)
  }
}
