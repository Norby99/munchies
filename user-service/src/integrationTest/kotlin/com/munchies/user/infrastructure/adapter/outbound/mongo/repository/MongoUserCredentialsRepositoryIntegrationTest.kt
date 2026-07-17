package com.munchies.user.infrastructure.adapter.outbound.mongo.repository

import com.munchies.user.domain.model.UserCredentials
import com.munchies.user.domain.model.UserId
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.micronaut.context.ApplicationContext
import org.junit.jupiter.api.*
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.mongodb.MongoDBContainer

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MongoUserCredentialsRepositoryIntegrationTest {
  companion object {
    @Container
    @JvmStatic
    private val mongo = MongoDBContainer("mongo:7.0")
  }

  private lateinit var context: ApplicationContext
  private lateinit var repository: MongoUserCredentialsRepository

  @BeforeAll
  fun setup() {
    context = ApplicationContext.run(
      mapOf(
        "mongodb.uri" to "${mongo.connectionString}/user-service",
        "mongodb.package-names[0]" to
          "com.munchies.user.infrastructure.adapter.outbound.mongo.document",
      ),
      "prod",
    )
    repository = context.getBean(MongoUserCredentialsRepository::class.java)
  }

  @AfterEach
  fun cleanup() {
    context.getBean(MongoCrudUserCredentialsRepository::class.java).deleteAll()
  }

  @AfterAll
  fun tearDown() {
    context.close()
  }

  @Test
  fun `find by id should return null on credentials not found `() {
    repository.findById(UserId("missing-id")).shouldBeNull()
  }

  @Test
  fun `find by id should return credentials on credentials found `() {
    val id = UserId("found-id")
    val credentials = UserCredentials(id = id, passwordHash = "hash", salt = "salt")
    repository.save(credentials)

    val found = repository.findById(id)
    found.shouldNotBeNull()
    found shouldBe credentials.copy(loginAttempts = 0, lastLogin = 0L, lockedUntil = -1L)
  }

  @Test
  fun `save should reset login related fields`() {
    val id = UserId("save-id")
    val credentials = UserCredentials(
      id = id,
      passwordHash = "hash",
      salt = "salt",
      loginAttempts = 5,
      lockedUntil = 9999L,
      lastLogin = 8888L,
    )

    repository.save(credentials)

    repository.findById(
      id,
    ) shouldBe credentials.copy(loginAttempts = 0, lastLogin = 0L, lockedUntil = -1L)
  }

  @Test
  fun `updates to credentials should persist and keep old hash when empty`() {
    val id = UserId("update-id")
    val original = UserCredentials(
      id = id,
      passwordHash = "orig-hash",
      salt = "orig-salt",
    )
    repository.save(original)

    val toUpdate = UserCredentials(
      id = id,
      passwordHash = "",
      salt = "",
      loginAttempts = 2,
      lockedUntil = 7777L,
      lastLogin = 6666L,
    )

    repository.update(toUpdate)

    val updated = repository.findById(id)
    updated.shouldNotBeNull()
    updated shouldBe UserCredentials(
      id = id,
      passwordHash = "orig-hash",
      salt = "orig-salt",
      loginAttempts = 2,
      lockedUntil = 7777L,
      lastLogin = 6666L,
    )
  }

  @Test
  fun `delete function removes credentials from db`() {
    val id = UserId("delete-id")
    val credentials = UserCredentials(id = id, passwordHash = "hash", salt = "salt")
    repository.save(credentials)
    val found = repository.findById(id)
    found.shouldNotBeNull()

    repository.delete(credentials)
    val deleted = repository.findById(id)
    deleted.shouldBeNull()
  }
}
