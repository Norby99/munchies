package com.munchies.user.adapter.outbound.memory

import com.munchies.commons.repository.InMemoryRepository
import com.munchies.user.domain.model.User
import com.munchies.user.domain.model.UserId
import com.munchies.user.infrastructure.adapter.outbound.memory.MemoryUserRepository
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MemoryUserRepositoryTest {

  fun createMemoryUserRepository(): MemoryUserRepository {
    return object : MemoryUserRepository {
      override val repository: InMemoryRepository<UserId, User>
        get() = object : InMemoryRepository<UserId, User>() {
          override fun create(): UserId {
            val id = UserId()
            this.save(User(id))
            return id
          }
        }
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
    val id = repository.create()
    repository.findById(id)?.shouldBeEqual(User(id))
  }

  @Test
  fun save() {
    val user = User(UserId())
    repository.save(user)
    repository.findById(user.id)?.shouldBeEqual(user)
  }

  @Test
  fun update() {
    // TODO
  }

  @Test
  fun delete() {
    val user = User(UserId())
    repository.save(user)
    repository.delete(user)
    repository.findById(user.id) shouldBe null
  }

  @Test
  fun create() {
    val id = repository.create()
    repository.findById(id)?.shouldBeEqual(User(id))
  }
}
