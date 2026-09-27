package com.munchies.suggestion.infrastructure.adapter.inbound.web.controller

import com.munchies.commons.domain.port.ValidationException
import com.munchies.commons.infrastructure.adapter.ErrorResponse
import com.munchies.suggestion.infrastructure.adapter.inbound.web.controller.exception.EngineTimeoutException
import com.munchies.suggestion.infrastructure.adapter.inbound.web.controller.exception.FactoryException
import com.munchies.suggestion.infrastructure.adapter.inbound.web.controller.exception.MalformedResponseException
import com.munchies.suggestion.infrastructure.adapter.inbound.web.controller.exception.UnexpectedException
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.server.exceptions.ExceptionHandler
import jakarta.inject.Singleton

@Singleton
class EngineTimeoutExceptionHandler :
  ExceptionHandler<EngineTimeoutException, HttpResponse<ErrorResponse>> {
  override fun handle(
    request: HttpRequest<*>,
    exception: EngineTimeoutException,
  ): HttpResponse<ErrorResponse> {
    return HttpResponse.status<ErrorResponse>(HttpStatus.UNAUTHORIZED)
      .body(
        ErrorResponse(
          result = exception.message ?: "Unauthorized",
          code = HttpStatus.UNAUTHORIZED.code,
        ),
      )
  }
}

@Singleton
class MalformedResponseExceptionHandler :
  ExceptionHandler<MalformedResponseException, HttpResponse<ErrorResponse>> {
  override fun handle(
    request: HttpRequest<*>,
    exception: MalformedResponseException,
  ): HttpResponse<ErrorResponse> {
    return HttpResponse.status<ErrorResponse>(HttpStatus.NOT_FOUND)
      .body(
        ErrorResponse(
          result = exception.message ?: "Resource not found",
          code = HttpStatus.NOT_FOUND.code,
        ),
      )
  }
}

@Singleton
class ValidationExceptionHandler :
  ExceptionHandler<ValidationException, HttpResponse<ErrorResponse>> {
  override fun handle(
    request: HttpRequest<*>,
    exception: ValidationException,
  ): HttpResponse<ErrorResponse> {
    return HttpResponse.status<ErrorResponse>(HttpStatus.BAD_REQUEST)
      .body(
        ErrorResponse(
          result = exception.message ?: "Request validation failed",
          code = HttpStatus.BAD_REQUEST.code,
        ),
      )
  }
}

@Singleton
class FactoryExceptionHandler :
  ExceptionHandler<FactoryException, HttpResponse<ErrorResponse>> {
  override fun handle(
    request: HttpRequest<*>,
    exception: FactoryException,
  ): HttpResponse<ErrorResponse> {
    return HttpResponse.status<ErrorResponse>(HttpStatus.BAD_REQUEST)
      .body(
        ErrorResponse(
          result = exception.message ?: "Factory validation failed",
          code = HttpStatus.BAD_REQUEST.code,
        ),
      )
  }
}

@Singleton
class UnexpectedExceptionHandler :
  ExceptionHandler<UnexpectedException, HttpResponse<ErrorResponse>> {
  override fun handle(
    request: HttpRequest<*>,
    exception: UnexpectedException,
  ): HttpResponse<ErrorResponse> {
    return HttpResponse.status<ErrorResponse>(HttpStatus.INTERNAL_SERVER_ERROR)
      .body(
        ErrorResponse(
          result = exception.message ?: "Unexpected result",
          code = HttpStatus.INTERNAL_SERVER_ERROR.code,
        ),
      )
  }
}
