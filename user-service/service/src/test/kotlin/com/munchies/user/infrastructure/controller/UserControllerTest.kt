package com.munchies.user.infrastructure.controller

import com.munchies.user.domain.model.UserId
import com.munchies.user.infrastructure.config.UserServiceConfig
import com.munchies.user.infrastructure.service.Service
import com.munchies.user.presentation.UserClient
import com.munchies.user.presentation.dto.UserDTO
import com.munchies.user.presentation.toDomain
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.serde.annotation.SerdeImport
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.findAnnotations
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

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
  fun `controller should throw exception for getUser not found`() {
    val service: Service.ControllerService = mock()
    val controller = UserController(service)

    val fakeId = UserId("???")

    whenever(service.getUser(fakeId)).thenReturn(null)

    shouldThrow<UserClient.Companion.UserNotFoundException> {
      controller.getUser(fakeId.value)
    }

    verify(service).getUser(fakeId)
  }

  @Test
  fun `HttpController should return a correct user when getUser`() {
    val fakeId = UserId("???")
    val fakeUserDTO = UserDTO(id = fakeId.value)
    val service: Service.ControllerService = mock<Service.ControllerService>()
    val controller = UserController(service)

    whenever(service.getUser(fakeId)).thenReturn(fakeUserDTO.toDomain())

    val response = controller.getUser(fakeId.value)
    response.status shouldBe HttpStatus.OK

    response.body() shouldBe fakeUserDTO
  }

  @Test
  fun `HttpController should return not found for non existing user ids`() {
    shouldThrow<HttpClientResponseException> {
      client.toBlocking().retrieve("???")
    }.status shouldBe HttpStatus.NOT_FOUND
  }
}
