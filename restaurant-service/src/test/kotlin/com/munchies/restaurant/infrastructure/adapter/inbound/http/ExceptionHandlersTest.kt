package com.munchies.restaurant.infrastructure.adapter.inbound.http

import com.munchies.restaurant.infrastructure.adapter.inbound.http.exception.ConflictException
import com.munchies.restaurant.infrastructure.adapter.inbound.http.exception.NotFoundException
import com.munchies.restaurant.infrastructure.adapter.inbound.http.exception.UnauthorizedException
import com.munchies.restaurant.infrastructure.adapter.inbound.http.exception.ValidationException
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ExceptionHandlersTest {

  private val request: HttpRequest<*> = mockk()

  @Test
  fun `UnauthorizedExceptionHandler should map the exception to a 401 with its message`() {
    val response =
      UnauthorizedExceptionHandler().handle(request, UnauthorizedException("not allowed"))

    assertEquals(HttpStatus.UNAUTHORIZED, response.status)
    assertEquals("not allowed", response.body().result)
  }

  @Test
  fun `NotFoundExceptionHandler should map the exception to a 404 with its message`() {
    val response = NotFoundExceptionHandler().handle(request, NotFoundException("not found"))

    assertEquals(HttpStatus.NOT_FOUND, response.status)
    assertEquals("not found", response.body().result)
  }

  @Test
  fun `ValidationExceptionHandler should map the exception to a 422 with its message`() {
    val response = ValidationExceptionHandler().handle(request, ValidationException("invalid"))

    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.status)
    assertEquals("invalid", response.body().result)
  }

  @Test
  fun `ConflictExceptionHandler should map the exception to a 409 with its message`() {
    val response = ConflictExceptionHandler().handle(request, ConflictException("conflict"))

    assertEquals(HttpStatus.CONFLICT, response.status)
    assertEquals("conflict", response.body().result)
  }
}
