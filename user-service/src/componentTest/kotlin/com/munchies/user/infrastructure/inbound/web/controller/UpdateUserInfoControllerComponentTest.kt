package com.munchies.user.infrastructure.inbound.web.controller

import com.munchies.user.fixtures.UserFixtures
import com.munchies.user.infrastructure.adapter.dto.factory.UserDTOFactory.toDTO
import com.munchies.user.infrastructure.adapter.inbound.request.UpdateUserInfoRequest
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
class UpdateUserInfoControllerComponentTest : BaseUserController() {

  @Inject
  lateinit var userRepository: MongoUserRepository

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
  fun `bad request when request is invalid`() {
    val request = UpdateUserInfoRequest(
      UserFixtures.exampleUser.toDTO().copy(id = ""),
    )
    val response = assertThrows(HttpClientResponseException::class.java) {
      httpCalls.patch(
        mapper.writeValueAsString(request),
        UserServiceConfig.UPDATE_USER_INFO_PATH,
      )
    }

    response.status shouldBe HttpStatus.BAD_REQUEST
  }

  @Test
  fun `bad request when factory is invalid`() {
    val request = UpdateUserInfoRequest(
      UserFixtures.exampleUser.toDTO().copy(role = "Bro"),
    )
    val response = assertThrows(HttpClientResponseException::class.java) {
      httpCalls.patch(
        mapper.writeValueAsString(request),
        UserServiceConfig.UPDATE_USER_INFO_PATH,
      )
    }

    response.status shouldBe HttpStatus.BAD_REQUEST
  }

  @Test
  fun `not found when user is not present`() {
    val request = UpdateUserInfoRequest(
      UserFixtures.exampleUser.toDTO(),
    )
    val response = assertThrows(HttpClientResponseException::class.java) {
      httpCalls.patch(
        mapper.writeValueAsString(request),
        UserServiceConfig.UPDATE_USER_INFO_PATH,
      )
    }

    response.status shouldBe HttpStatus.NOT_FOUND
  }

  @Test
  fun `ok when update is successful`() {
    val request = UpdateUserInfoRequest(
      UserFixtures.exampleUser.toDTO(),
    )

    userRepository.save(UserFixtures.exampleUser)

    val response =
      httpCalls.patch(
        mapper.writeValueAsString(request),
        UserServiceConfig.UPDATE_USER_INFO_PATH,
      )
    response.status shouldBe HttpStatus.OK
  }
}
