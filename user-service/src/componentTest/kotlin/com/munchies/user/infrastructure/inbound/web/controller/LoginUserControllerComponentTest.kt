package com.munchies.user.infrastructure.inbound.web.controller

import com.munchies.user.domain.model.UserCredentials
import com.munchies.user.domain.port.PasswordHasher
import com.munchies.user.domain.port.UserCredentialsRepository
import com.munchies.user.domain.port.addOneHour
import com.munchies.user.domain.port.defaultTimeProvider
import com.munchies.user.fixtures.UserFixtures
import com.munchies.user.infrastructure.adapter.inbound.request.LoginUserRequest
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
class LoginUserControllerComponentTest : BaseUserController() {
  @Inject
  lateinit var userRepository: MongoUserRepository

  @Inject
  lateinit var userCredentialsRepository: UserCredentialsRepository

  @Inject
  lateinit var hasher: PasswordHasher

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
  fun `login returns bad request when receiving invalid request`() {
    val request = LoginUserRequest(
      "",
      "",
      "",
    )

    val response = assertThrows(HttpClientResponseException::class.java) {
      httpCalls.post(mapper.writeValueAsString(request), UserServiceConfig.LOGIN_USER_PATH)
    }
    response.status shouldBe HttpStatus.BAD_REQUEST
  }

  @Test
  fun `login returns bad request when user doesnt exist`() {
    val request = LoginUserRequest(
      "email",
      "username",
      "password",
    )

    val response = assertThrows(HttpClientResponseException::class.java) {
      httpCalls.post(mapper.writeValueAsString(request), UserServiceConfig.LOGIN_USER_PATH)
    }
    response.status shouldBe HttpStatus.BAD_REQUEST
  }

  @Test
  fun `login returns unauthorized when user is blocked`() {
    val user = UserFixtures.exampleUser
    val salt = hasher.generateSalt()
    val passwordHash = hasher.hash("password", salt)
    val credentials = UserCredentials(
      user.id,
      passwordHash,
      salt = salt,
      lockedUntil = defaultTimeProvider().addOneHour().addOneHour()(),
    )
    userRepository.save(user)
    userCredentialsRepository.save(credentials)
    userCredentialsRepository.update(credentials)

    val request = LoginUserRequest(
      user.profile.email.address,
      user.profile.username,
      passwordHash,
    )

    val response = assertThrows(HttpClientResponseException::class.java) {
      httpCalls.post(mapper.writeValueAsString(request), UserServiceConfig.LOGIN_USER_PATH)
    }
    println(response.response.body())
    response.status shouldBe HttpStatus.UNAUTHORIZED
  }

  @Test
  fun `login returns ok with correct credentials`() {
    val user = UserFixtures.exampleUser
    val salt = hasher.generateSalt()
    val passwordHash = hasher.hash("password", salt)
    val credentials = UserCredentials(
      user.id,
      passwordHash,
      salt = salt,
    )
    userRepository.save(user)
    userCredentialsRepository.save(credentials)
    userCredentialsRepository.update(credentials)

    val request = LoginUserRequest(
      user.profile.email.address,
      user.profile.username,
      "password",
    )

    val response = httpCalls.post(
      mapper.writeValueAsString(request),
      UserServiceConfig.LOGIN_USER_PATH,
    )
    response.status shouldBe HttpStatus.OK
  }
}
