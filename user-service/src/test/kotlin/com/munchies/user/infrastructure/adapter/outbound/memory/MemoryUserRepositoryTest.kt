package com.munchies.user.infrastructure.adapter.outbound.memory

import com.munchies.commons.repository.InMemoryRepository
import com.munchies.user.domain.model.*
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MemoryUserRepositoryTest {

  fun createMemoryUserRepository(): MemoryUserRepository {
    return object : MemoryUserRepository {
      val map = mutableMapOf<UserId, User>()
      override val repository: InMemoryRepository<UserId, User>
        get() = object : InMemoryRepository<UserId, User>(map) {
        }

      override fun findByEmail(email: String): User? = map.values.find {
        it.profile.email.address == email
      }

      override fun findByUsername(username: String): User? =
        map.values.find { it.profile.username == username }
    }
  }

  lateinit var repository: MemoryUserRepository

  @BeforeEach
  fun setUp() {
    repository = createMemoryUserRepository()
  }

  @Test
  fun `repository doesnt find non-existing id`() {
    repository.findById(UserId()) shouldBe null
  }

  @Test
  fun `repository correctly finds existing id`() {
    repository.save(exampleUser)
    repository.findById(exampleUserId)?.shouldBeEqual(exampleUser)
  }

  @Test
  fun save() {
    val user = exampleUser
    repository.save(user)
    repository.findById(user.id)?.shouldBeEqual(user)
  }

  @Test
  fun update() {
    val user = exampleUser
    repository.save(user)

    val updatedUser = user.update(
      id = user.id,
      profile = user.profile.copy(username = "Updated Name"),
    )
    repository.update(updatedUser)

    repository.findById(user.id)?.shouldBeEqual(updatedUser)
  }

  @Test
  fun delete() {
    val user = exampleUser
    repository.save(user)
    repository.delete(user)
    repository.findById(user.id) shouldBe null
  }
}
