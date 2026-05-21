package com.munchies.user.infrastructure.adapter.outbound.memory

import com.munchies.commons.repository.InMemoryRepository
import com.munchies.user.domain.model.UserCredentials
import com.munchies.user.domain.model.UserId
import io.kotest.matchers.shouldBe
import kotlin.compareTo
import org.junit.jupiter.api.Test

class MemoryUserCredentialsRepositoryTest {
  @Test
  fun `findById should return null when credentials do not exist`() {
    val memoryRepository = createMemoryUserCredentialsRepository()

    memoryRepository.findById(UserId("missing-id")) shouldBe null
  }

  @Test
  fun `save should persist credentials with default security state`() {
    val memoryRepository = createMemoryUserCredentialsRepository()
    val credentials = UserCredentials(
      id = UserId("user-credentials-id"),
      passwordHash = "hash-1",
      salt = "salt-1",
      loginAttempts = 7,
      lockedUntil = 9999L,
      lastLogin = 1234L,
    )

    memoryRepository.save(credentials)

    memoryRepository.findById(credentials.id) shouldBe credentials.copy(
      loginAttempts = 0,
      lockedUntil = -1L,
      lastLogin = 0L,
    )
  }

  @Test
  fun `update should modify existing credentials and keep previous hash and salt`() {
    val memoryRepository = createMemoryUserCredentialsRepository()
    val id = UserId("update-credentials-id")
    val stored = UserCredentials(
      id = id,
      passwordHash = "original-hash",
      salt = "original-salt",
    )
    memoryRepository.save(stored)

    memoryRepository.update(
      UserCredentials(
        id = id,
        passwordHash = "",
        salt = "",
        loginAttempts = 4,
        lockedUntil = 5555L,
        lastLogin = 6666L,
      ),
    )

    memoryRepository.findById(id) shouldBe UserCredentials(
      id = id,
      passwordHash = "original-hash",
      salt = "original-salt",
      loginAttempts = 4,
      lockedUntil = 5555L,
      lastLogin = 6666L,
    )
  }

  @Test
  fun `update should modify existing credentials using provided hash and salt`() {
    val memoryRepository = createMemoryUserCredentialsRepository()
    val id = UserId("update-with-new-values-id")
    memoryRepository.save(
      UserCredentials(
        id = id,
        passwordHash = "old-hash",
        salt = "old-salt",
      ),
    )

    memoryRepository.update(
      UserCredentials(
        id = id,
        passwordHash = "new-hash",
        salt = "new-salt",
        loginAttempts = 1,
        lockedUntil = 2000L,
        lastLogin = 3000L,
      ),
    )

    memoryRepository.findById(id) shouldBe UserCredentials(
      id = id,
      passwordHash = "new-hash",
      salt = "new-salt",
      loginAttempts = 1,
      lockedUntil = 2000L,
      lastLogin = 3000L,
    )
  }

  @Test
  fun `update should do nothing when credentials do not exist`() {
    val memoryRepository = createMemoryUserCredentialsRepository()

    memoryRepository.update(
      UserCredentials(
        id = UserId("absent-id"),
        passwordHash = "hash",
        salt = "salt",
        loginAttempts = 2,
        lockedUntil = 100L,
        lastLogin = 200L,
      ),
    )

    memoryRepository.findById(UserId("absent-id")) shouldBe null
  }

  @Test
  fun `delete should remove existing credentials`() {
    val memoryRepository = createMemoryUserCredentialsRepository()
    val credentials = UserCredentials(
      id = UserId("delete-id"),
      passwordHash = "hash",
      salt = "salt",
    )
    memoryRepository.save(credentials)

    memoryRepository.delete(credentials)

    memoryRepository.findById(credentials.id) shouldBe null
  }

  @Test
  fun `delete should not fail when credentials do not exist`() {
    val memoryRepository = createMemoryUserCredentialsRepository()
    val missingCredentials = UserCredentials(
      id = UserId("missing-delete-id"),
      passwordHash = "hash",
      salt = "salt",
    )

    memoryRepository.delete(missingCredentials)

    memoryRepository.findById(missingCredentials.id) shouldBe null
  }

  @Test
  fun `findByPredicate should return null when no credentials match the predicate`() {
    val memoryRepository = createMemoryUserCredentialsRepository()
    val credentials = UserCredentials(
      id = UserId("non-matching-id"),
      passwordHash = "hash",
      salt = "salt",
      loginAttempts = 1,
    )
    memoryRepository.save(credentials)

    val result = memoryRepository.findByPredicate { it.loginAttempts > 10 }

    result shouldBe null
  }

  @Test
  fun `delete should not remove credentials when id does not match`() {
    val memoryRepository = createMemoryUserCredentialsRepository()
    val credentials = UserCredentials(
      id = UserId("existing-id"),
      passwordHash = "hash",
      salt = "salt",
    )
    memoryRepository.save(credentials)

    memoryRepository.delete(
      UserCredentials(
        id = UserId("non-existing-id"),
        passwordHash = "hash",
        salt = "salt",
      ),
    )

    memoryRepository.findById(credentials.id) shouldBe credentials
  }

  private fun createMemoryUserCredentialsRepository(): MemoryUserCredentialsRepository =
    object : MemoryUserCredentialsRepository {
      override val repository: InMemoryRepository<UserId, UserCredentials> =
        object : InMemoryRepository<UserId, UserCredentials>() {}

      override fun findByPredicate(predicate: (UserCredentials) -> Boolean): UserCredentials? =
        repository.findByPredicate { predicate(it) }
    }
}
