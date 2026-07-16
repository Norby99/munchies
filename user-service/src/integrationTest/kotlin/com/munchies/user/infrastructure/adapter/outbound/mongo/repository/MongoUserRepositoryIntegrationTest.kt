package com.munchies.user.infrastructure.adapter.outbound.mongo.repository

import com.munchies.user.domain.model.User
import com.munchies.user.domain.model.UserId
import com.munchies.user.fixtures.UserFixtures
import io.kotest.matchers.equals.shouldNotBeEqual
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.micronaut.context.ApplicationContext
import org.junit.jupiter.api.*
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.mongodb.MongoDBContainer

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MongoUserRepositoryIntegrationTest {

  companion object {
    @Container
    @JvmStatic
    private val mongo = MongoDBContainer("mongo:7.0")
  }

  private lateinit var context: ApplicationContext
  private lateinit var repository: MongoUserRepository

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
    repository = context.getBean(MongoUserRepository::class.java)
  }

  @AfterEach
  fun cleanup() {
    context.getBean(MongoCrudUserRepository::class.java).deleteAll()
  }

  @AfterAll
  fun tearDown() {
    context.close()
  }

  @Test
  fun `find by id should return null on user not found `() {
    repository.findById(UserId()).shouldBeNull()
  }

  @Test
  fun `find by id should return user on user found `() {
    val user = UserFixtures.exampleUser
    repository.save(user)
    val found = repository.findById(user.id)
    found.shouldNotBeNull()
    found shouldBe user
  }

  @Test
  fun `updates to a user should persist`() {
    val user = UserFixtures.exampleUser
    repository.save(user)
    val found = repository.findById(user.id)
    found.shouldNotBeNull()
    found shouldBe user

    val newUser =
      User.factory.create(
        user.id.value,
        user.profile.copy(username = "new-username"),
      ).shouldBeInstanceOf<User.Companion.UserFactory.UserFactoryResult.Success>().user

    repository.update(newUser)

    val updatedUser = repository.findById(user.id)
    updatedUser.shouldNotBeNull()
    updatedUser.profile.username shouldBe newUser.profile.username
    updatedUser.profile.username shouldNotBeEqual user.profile.username
  }

  @Test
  fun `delete function removes user from db`()  {
    val user = UserFixtures.exampleUser
    repository.save(user)
    val found = repository.findById(user.id)
    found.shouldNotBeNull()
    found shouldBe user

    repository.delete(user)
    val deleted = repository.findById(user.id)
    deleted.shouldBeNull()
  }

  @Test
  fun `a user may be found by its username or email`()  {
    val user = UserFixtures.exampleUser
    repository.save(user)

    repository.findByEmail(user.profile.email.address) shouldBe user
    repository.findByUsername(user.profile.username) shouldBe user
  }
}
