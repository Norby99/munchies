package com.munchies.user.infrastructure.inbound.web.controller

import com.munchies.user.domain.port.PasswordHasher
import com.munchies.user.infrastructure.adapter.inbound.request.VerifyEmailRequest
import com.munchies.user.infrastructure.adapter.inbound.web.config.UserServiceConfig
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
class VerifyEmailControllerComponentTest : BaseUserController() {
  @Inject
  lateinit var userRepository: MongoUserRepository

  @Inject
  lateinit var mongoCrudUserCredentialsRepository: MongoCrudUserCredentialsRepository

  @Inject
  lateinit var mongoCrudUserRepository: MongoCrudUserRepository

  @Inject
  lateinit var passwordHasher: PasswordHasher

  @AfterEach
  fun cleanupMongo() {
    mongoCrudUserCredentialsRepository.deleteAll()
    mongoCrudUserRepository.deleteAll()
  }

  @Test
  fun `verify email should return bad request when request is invalid`() {
    val request = VerifyEmailRequest("", "")
    val response = assertThrows(HttpClientResponseException::class.java) {
      httpCalls.post(mapper.writeValueAsString(request), UserServiceConfig.VERIFY_EMAIL_PATH)
    }
    response.status shouldBe HttpStatus.BAD_REQUEST
  }

  @Test
  fun `verify email should return not found when user does not exist`() {
    val request = VerifyEmailRequest("non-existent-id", "otk-token")
    val response = assertThrows(HttpClientResponseException::class.java) {
      httpCalls.post(mapper.writeValueAsString(request), UserServiceConfig.VERIFY_EMAIL_PATH)
    }
    response.status shouldBe HttpStatus.NOT_FOUND
  }
}
