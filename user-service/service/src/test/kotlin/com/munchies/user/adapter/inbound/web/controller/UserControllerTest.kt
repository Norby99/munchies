package com.munchies.user.adapter.inbound.web.controller

import com.munchies.user.adapter.config.UserServiceConfig
import com.munchies.user.application.port.inbound.GetUserQuery
import com.munchies.user.domain.model.UserId
import com.munchies.user.presentation.dto.UserDTO
import com.munchies.user.presentation.toDomain
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.serde.annotation.SerdeImport
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.findAnnotations
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@MicronautTest
class UserControllerTest {

  @Inject
  lateinit var server: EmbeddedServer

  @Inject
  @field:Client(UserServiceConfig.SERVICE_PATH)
  lateinit var client: HttpClient

  @Test
  fun `controller is @Controller annotated`() {
    val controller = UserController::class
    controller.findAnnotation<Controller>() shouldNotBe null
  }

  @Test
  fun `controller should have @SerdeImport for DTOs`() {
    val controller = UserController::class
    val serdeImport = controller.findAnnotations<SerdeImport>()
    serdeImport shouldNotBe null
    serdeImport.shouldNotBeEmpty()
  }

  @Test
  fun `controller should respond NotFound for getUser not found`() {
    val userId = UserId("nonexistent")
    val userUseCase = mock<GetUserQuery> {
      on { execute(userId) } doReturn GetUserQuery.Companion.GetUserResult.NotFound
    }
    val controller = UserController(userUseCase)

    val res = controller.getUser(userId.value)
    res.status shouldBe HttpStatus.NOT_FOUND
    verify(userUseCase).execute(userId)
  }

  @Test
  fun `controller should return a correct user when getUser`() {
    val userId = UserId("very-real-id")
    val userDTO = UserDTO(id = userId.value)
    val userUseCase = mock<GetUserQuery> {
      on {
        execute(
          userId,
        )
      } doReturn GetUserQuery.Companion.GetUserResult.Success(userDTO.toDomain())
    }

    val controller = UserController(userUseCase)

    val res = controller.getUser(userId.value)
    res.status shouldBe HttpStatus.OK
    res.body() shouldNotBe null
    res.body().id shouldBe userId.value
    verify(userUseCase).execute(userId)
  }
}
