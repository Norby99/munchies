package com.munchies.user.infrastructure.controller

import com.munchies.user.infrastructure.config.UserServiceConfig
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldNotBe
import io.micronaut.http.annotation.Controller
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import kotlin.reflect.full.findAnnotation
import org.junit.jupiter.api.Test

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
  fun `should return not found for non existing user ids`() {
    shouldThrow<HttpClientResponseException> { client.toBlocking().retrieve("???") }
  }
}
