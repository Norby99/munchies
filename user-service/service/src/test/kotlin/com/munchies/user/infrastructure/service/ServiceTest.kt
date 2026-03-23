package com.munchies.user.infrastructure.service

import com.munchies.user.domain.model.UserId
import io.kotest.matchers.shouldBe
import io.micronaut.context.ApplicationContext
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test

@MicronautTest
class ServiceTest {

  val service: Service.ControllerService = ApplicationContext.run().getBean(
    Service.ControllerService::class.java,
  )

  @Test
  fun `should return null for non existing user ids`() {
    service.getUser(UserId("???")) shouldBe null
  }
}
