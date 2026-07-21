package com.munchies.user.infrastructure.inbound.web.controller

import com.munchies.user.fixtures.UserFixtures
import com.munchies.user.infrastructure.adapter.outbound.mongo.repository.MongoCrudUserCredentialsRepository
import com.munchies.user.infrastructure.adapter.outbound.mongo.repository.MongoUserRepository
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

@MicronautTest(environments = ["prod"], transactional = false)
class GetUserControllerComponentTest : BaseUserController() {
  @Inject
  lateinit var userRepository: MongoUserRepository

  @Inject
  lateinit var mongoCrudUserCredentialsRepository: MongoCrudUserCredentialsRepository

  @AfterEach
  fun cleanupMongo() {
    mongoCrudUserCredentialsRepository.deleteAll()
  }

  @Test
  fun `get user returns not found when user is not present`() {
    val user = UserFixtures.exampleUser

    val response = assertThrows(HttpClientResponseException::class.java) {
      httpCalls.get(
        user.id.value,
      )
    }

    response.status shouldBe HttpStatus.NOT_FOUND
  }

  @Test
  fun `get user returns user when present`() {
    val user = UserFixtures.exampleUser
    userRepository.save(user)

    userRepository.findById(user.id).shouldNotBeNull()

    val response =
      httpCalls.get(
        user.id.value,
      )

    response.status shouldBe HttpStatus.OK
  }
}
