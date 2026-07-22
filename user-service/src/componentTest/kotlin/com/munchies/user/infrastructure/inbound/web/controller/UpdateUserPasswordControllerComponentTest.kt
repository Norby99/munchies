package com.munchies.user.infrastructure.inbound.web.controller

import com.munchies.user.domain.model.UserCredentials
import com.munchies.user.domain.port.PasswordHasher
import com.munchies.user.domain.port.UserCredentialsRepository
import com.munchies.user.domain.port.addOneHour
import com.munchies.user.domain.port.defaultTimeProvider
import com.munchies.user.fixtures.UserFixtures
import com.munchies.user.infrastructure.adapter.inbound.request.UpdateUserPasswordRequest
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
class UpdateUserPasswordControllerComponentTest : BaseUserController() {

  @Inject
  lateinit var userRepository: MongoUserRepository

  @Inject
  lateinit var userCredentialsRepository: UserCredentialsRepository

  @Inject
  lateinit var mongoCrudUserCredentialsRepository: MongoCrudUserCredentialsRepository

  @Inject
  lateinit var hasher: PasswordHasher

  @Inject
  lateinit var mongoCrudUserRepository: MongoCrudUserRepository

  @AfterEach
  fun cleanupMongo() {
    mongoCrudUserCredentialsRepository.deleteAll()
    mongoCrudUserRepository.deleteAll()
  }

  @Test
  fun `bad request when request is invalid`() {
    val request = UpdateUserPasswordRequest(
      UserFixtures.exampleUser.id.value,
      UserFixtures.exampleUser.profile.username,
      UserFixtures.exampleUser.profile.email.address,
      "",
      "",
    )
    val response = assertThrows(HttpClientResponseException::class.java) {
      httpCalls.patch(
        mapper.writeValueAsString(request),
        UserServiceConfig.UPDATE_USER_PASSWORD_PATH,
      )
    }

    response.status shouldBe HttpStatus.BAD_REQUEST
  }

  @Test
  fun `not found when user is not present`() {
    val request = UpdateUserPasswordRequest(
      UserFixtures.exampleUser.id.value,
      UserFixtures.exampleUser.profile.username,
      UserFixtures.exampleUser.profile.email.address,
      "password",
      "password",
    )
    val response = assertThrows(HttpClientResponseException::class.java) {
      httpCalls.patch(
        mapper.writeValueAsString(request),
        UserServiceConfig.UPDATE_USER_PASSWORD_PATH,
      )
    }

    response.status shouldBe HttpStatus.NOT_FOUND
  }

  @Test
  fun `unauthorized when user is locked`() {
    val user = UserFixtures.exampleUser
    val salt = hasher.generateSalt()
    val password = "password"
    val passwordHash = hasher.hash(password, "")
    val storedPasswordHash = hasher.hash(passwordHash, salt)
    val credentials = UserCredentials(
      user.id,
      storedPasswordHash,
      salt,
      lockedUntil = defaultTimeProvider().addOneHour().addOneHour()(),
    )

    userRepository.save(user)
    userCredentialsRepository.save(credentials)
    userCredentialsRepository.update(credentials)

    val request = UpdateUserPasswordRequest(
      user.id.value,
      username = user.profile.username,
      email = user.profile.email.address,
      passwordHash,
      "newPassword",
    )
    val response = assertThrows(HttpClientResponseException::class.java) {
      httpCalls.patch(
        request.toJson(),
        UserServiceConfig.UPDATE_USER_PASSWORD_PATH,
      )
    }

    response.status shouldBe HttpStatus.UNAUTHORIZED
  }

  @Test
  fun `unauthorized operation for different id provided`() {
    val user = UserFixtures.exampleUser
    val salt = hasher.generateSalt()
    val password = "password"
    val passwordHash = hasher.hash(password, "")
    val storedPasswordHash = hasher.hash(passwordHash, salt)
    val credentials = UserCredentials(
      user.id,
      storedPasswordHash,
      salt,
    )

    userRepository.save(user)
    userCredentialsRepository.save(credentials)
    userCredentialsRepository.update(credentials)

    val request = UpdateUserPasswordRequest(
      "fake id",
      username = user.profile.username,
      email = user.profile.email.address,
      passwordHash,
      "newPassword",
    )
    val response = assertThrows(HttpClientResponseException::class.java) {
      httpCalls.patch(
        request.toJson(),
        UserServiceConfig.UPDATE_USER_PASSWORD_PATH,
      )
    }

    response.status shouldBe HttpStatus.UNAUTHORIZED
  }

  @Test
  fun `ok when update is successful`() {
    val user = UserFixtures.exampleUser
    val salt = hasher.generateSalt()
    val password = "password"
    val passwordHash = hasher.hash(password, "")
    val storedPasswordHash = hasher.hash(passwordHash, salt)
    val credentials = UserCredentials(
      user.id,
      storedPasswordHash,
      salt,
    )

    userRepository.save(user)
    userCredentialsRepository.save(credentials)
    userCredentialsRepository.update(credentials)

    val request = UpdateUserPasswordRequest(
      user.id.value,
      username = user.profile.username,
      email = user.profile.email.address,
      passwordHash,
      "newPassword",
    )
    val response = httpCalls.patch(
      request.toJson(),
      UserServiceConfig.UPDATE_USER_PASSWORD_PATH,
    )

    response.status shouldBe HttpStatus.OK
  }
}
