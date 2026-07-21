package com.munchies.user.infrastructure.inbound.web.controller

import com.munchies.user.infrastructure.adapter.dto.UserDTO
import com.munchies.user.infrastructure.adapter.inbound.request.RegisterUserRequest
import com.munchies.user.infrastructure.adapter.inbound.web.config.UserServiceConfig
import com.munchies.user.infrastructure.adapter.outbound.mongo.repository.MongoCrudUserCredentialsRepository
import com.munchies.user.infrastructure.adapter.outbound.mongo.repository.MongoUserRepository
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test

@MicronautTest(environments = ["prod"], transactional = false)
class RegisterUserControllerComponentTest : BaseUserController() {
  @Inject
  lateinit var userRepository: MongoUserRepository

  @Inject
  lateinit var mongoCrudUserCredentialsRepository: MongoCrudUserCredentialsRepository

  @AfterEach
  fun cleanupMongo() {
    mongoCrudUserCredentialsRepository.deleteAll()
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

    try {
      val response = httpCalls.post(request, UserServiceConfig.REGISTER_USER_PATH)
      response.status shouldBe HttpStatus.BAD_REQUEST
    } catch (e: HttpClientResponseException) {
      fail()
    }
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
  }

  @Test
  fun `register user should return unauthorized when already present`() {
  }

  @Test
  fun `register user should return ok`() {
  }
}
