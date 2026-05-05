package com.munchies.restaurant.domain.valueobject

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UserIdTest {

  @Test
  fun `should create user id with factory method`() {
    val id = UserId.of("test-user-id-456")

    assertEquals("test-user-id-456", id.value)
  }

  @Test
  fun `should generate unique id when created without parameter`() {
    val id1 = UserId()
    val id2 = UserId()

    assertNotEquals(id1.value, id2.value)
  }

  @Test
  fun `should throw exception when id is blank`() {
    assertThrows<IllegalArgumentException> {
      UserId.of("")
    }
  }

  @Test
  fun `should throw exception when id is only whitespace`() {
    assertThrows<IllegalArgumentException> {
      UserId.of("   ")
    }
  }

  @Test
  fun `should preserve id value after creation`() {
    val originalId = "user-67890"
    val id = UserId.of(originalId)

    assertEquals(originalId, id.value)
  }

  @Test
  fun `should create two ids with same value equal`() {
    val id1 = UserId.of("same-user-id")
    val id2 = UserId.of("same-user-id")

    assertEquals(id1, id2)
  }

  @Test
  fun `should create ids with UUID format`() {
    val id1 = UserId()
    val id2 = UserId()

    assertEquals(36, id1.value.length)
    assertEquals(36, id2.value.length)
  }
}
