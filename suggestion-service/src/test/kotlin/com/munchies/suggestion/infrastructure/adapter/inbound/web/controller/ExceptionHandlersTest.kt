package com.munchies.suggestion.infrastructure.adapter.inbound.web.controller

import com.munchies.commons.domain.port.ValidationException
import com.munchies.suggestion.infrastructure.adapter.inbound.web.controller.exception.EngineTimeoutException
import com.munchies.suggestion.infrastructure.adapter.inbound.web.controller.exception.FactoryException
import com.munchies.suggestion.infrastructure.adapter.inbound.web.controller.exception.MalformedResponseException
import com.munchies.suggestion.infrastructure.adapter.inbound.web.controller.exception.UnexpectedException
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class ExceptionHandlersTest {

  private val request = mock<HttpRequest<*>>()

  @Test
  fun `EngineTimeoutExceptionHandler handles exception`() {
    val handler = EngineTimeoutExceptionHandler()
    val response = handler.handle(request, EngineTimeoutException("timed out"))
    response.status shouldBe HttpStatus.INTERNAL_SERVER_ERROR
    response.body()?.code shouldBe HttpStatus.INTERNAL_SERVER_ERROR.code
    response.body()?.result shouldBe "timed out"

    val defaultResponse = handler.handle(request, EngineTimeoutException(""))
    defaultResponse.status shouldBe HttpStatus.INTERNAL_SERVER_ERROR
  }

  @Test
  fun `MalformedResponseExceptionHandler handles exception`() {
    val handler = MalformedResponseExceptionHandler()
    val response = handler.handle(request, MalformedResponseException("malformed"))
    response.status shouldBe HttpStatus.UNPROCESSABLE_ENTITY
    response.body()?.code shouldBe HttpStatus.UNPROCESSABLE_ENTITY.code
    response.body()?.result shouldBe "malformed"
  }

  @Test
  fun `ValidationExceptionHandler handles exception`() {
    val handler = ValidationExceptionHandler()
    val response = handler.handle(request, ValidationException("invalid field"))
    response.status shouldBe HttpStatus.BAD_REQUEST
    response.body()?.code shouldBe HttpStatus.BAD_REQUEST.code
    response.body()?.result shouldBe "invalid field"
  }

  @Test
  fun `FactoryExceptionHandler handles exception`() {
    val handler = FactoryExceptionHandler()
    val response = handler.handle(request, FactoryException("factory error"))
    response.status shouldBe HttpStatus.BAD_REQUEST
    response.body()?.code shouldBe HttpStatus.BAD_REQUEST.code
    response.body()?.result shouldBe "factory error"
  }

  @Test
  fun `UnexpectedExceptionHandler handles exception`() {
    val handler = UnexpectedExceptionHandler()
    val response = handler.handle(request, UnexpectedException("something broke"))
    response.status shouldBe HttpStatus.INTERNAL_SERVER_ERROR
    response.body()?.code shouldBe HttpStatus.INTERNAL_SERVER_ERROR.code
    response.body()?.result shouldBe "something broke"
  }
}
