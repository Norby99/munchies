package com.munchies.user.infrastructure.inbound.web.controller

import com.munchies.user.domain.port.UserCredentialsRepository
import com.munchies.user.fixtures.UserFixtures
import com.munchies.user.infrastructure.adapter.outbound.mongo.repository.MongoCrudUserCredentialsRepository
import com.munchies.user.infrastructure.adapter.outbound.mongo.repository.MongoCrudUserRepository
import com.munchies.user.infrastructure.adapter.outbound.mongo.repository.MongoUserRepository
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

@MicronautTest(environments = ["prod"], transactional = false)
class DeleteUserControllerComponentTest : BaseUserController() {
  @Inject
  lateinit var userRepository: MongoUserRepository

  @Inject
  lateinit var userCredentialsRepository: UserCredentialsRepository

  @Inject
  lateinit var mongoCrudUserCredentialsRepository: MongoCrudUserCredentialsRepository

  @Inject
  lateinit var mongoCrudUserRepository: MongoCrudUserRepository

  @AfterEach
  fun cleanupMongo() {
    mongoCrudUserCredentialsRepository.deleteAll()
    mongoCrudUserRepository.deleteAll()
  }

  @Test
  fun `delete user returns not found when user is not present`() {
    val user = UserFixtures.exampleUser

    val response = assertThrows(HttpClientResponseException::class.java) {
      httpCalls.delete(
        user.id.value,
      )
    }
    response.status shouldBe HttpStatus.NOT_FOUND
  }

  @Test
  fun `delete user returns ok when user is found`() {
    val user = UserFixtures.exampleUser

    userRepository.save(user)

    val response = httpCalls.delete(
      user.id.value,
    )

    response.status shouldBe HttpStatus.OK
  }
}
