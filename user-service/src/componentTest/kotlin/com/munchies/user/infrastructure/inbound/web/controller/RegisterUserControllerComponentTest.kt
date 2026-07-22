package com.munchies.user.infrastructure.inbound.web.controller

import com.munchies.user.fixtures.UserFixtures
import com.munchies.user.infrastructure.adapter.dto.UserDTO
import com.munchies.user.infrastructure.adapter.dto.factory.UserDTOFactory.toDTO
import com.munchies.user.infrastructure.adapter.inbound.request.RegisterUserRequest
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
class RegisterUserControllerComponentTest : BaseUserController() {
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
  fun `invalid register user request should return bad request`() {
    val request = RegisterUserRequest(
      UserDTO(
        "",
        "",
        "",
        role = "CUSTOMER",
      ),
      "",
      "",
    )

    val response = assertThrows(HttpClientResponseException::class.java) {
      httpCalls.post(mapper.writeValueAsString(request), UserServiceConfig.REGISTER_USER_PATH)
    }
    response.status shouldBe HttpStatus.BAD_REQUEST
  }

  @Test
  fun `register user should return bad request when userdto is invalid`() {
    val request = RegisterUserRequest(
      UserDTO(
        "",
        "username",
        "email",
        role = "invalid-role",
      ),
      "password",
      "salt",
    )
    val response = assertThrows(HttpClientResponseException::class.java) {
      httpCalls.post(mapper.writeValueAsString(request), UserServiceConfig.REGISTER_USER_PATH)
    }
    response.status shouldBe HttpStatus.BAD_REQUEST
  }

  @Test
  fun `register user should return unauthorized when already present`() {
    val user = UserFixtures.exampleUser.toDTO()
    val request = RegisterUserRequest(
      user,
      "password",
      "saltvalue",
    )

    userRepository.save(UserFixtures.exampleUser)

    val response = assertThrows(HttpClientResponseException::class.java) {
      httpCalls.post(mapper.writeValueAsString(request), UserServiceConfig.REGISTER_USER_PATH)
    }
    response.status shouldBe HttpStatus.UNAUTHORIZED
  }

  @Test
  fun `register user should return ok`() {
    val user = UserFixtures.exampleUser.toDTO()
    val request = RegisterUserRequest(
      user,
      "password",
      "saltvalue",
    )
    val response = httpCalls.post(
      mapper.writeValueAsString(request),
      UserServiceConfig.REGISTER_USER_PATH,
    )
    response.status shouldBe HttpStatus.OK
  }
}
