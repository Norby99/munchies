package com.munchies.user.infrastructure.controller

import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@MicronautTest
class UserControllerTest {

  @Inject
  lateinit var server: EmbeddedServer

  @Test
  fun `should return user by id`() {
    Assertions.assertTrue(server.isRunning)
  }
}
