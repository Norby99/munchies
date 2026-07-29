package com.munchies.user.infrastructure.adapter.inbound.web.controller

import com.munchies.commons.domain.port.ValidationException
import com.munchies.commons.infrastructure.adapter.ErrorResponse
import com.munchies.user.infrastructure.adapter.inbound.web.controller.exception.FactoryException
import com.munchies.user.infrastructure.adapter.inbound.web.controller.exception.NotFoundException
import com.munchies.user.infrastructure.adapter.inbound.web.controller.exception.UnauthorizedException
import com.munchies.user.infrastructure.adapter.inbound.web.controller.exception.UnexpectedException
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.server.exceptions.ExceptionHandler
import jakarta.inject.Singleton

@Singleton
class UnauthorizedExceptionHandler :
  ExceptionHandler<UnauthorizedException, HttpResponse<ErrorResponse>> {
  override fun handle(
    request: HttpRequest<*>,
    exception: UnauthorizedException,
  ): HttpResponse<ErrorResponse> {
    return HttpResponse.status<ErrorResponse>(HttpStatus.UNAUTHORIZED)
      .body(ErrorResponse(exception.message ?: "Unauthorized"))
  }
}

@Singleton
class NotFoundExceptionHandler :
  ExceptionHandler<NotFoundException, HttpResponse<ErrorResponse>> {
  override fun handle(
    request: HttpRequest<*>,
    exception: NotFoundException,
  ): HttpResponse<ErrorResponse> {
    return HttpResponse.status<ErrorResponse>(HttpStatus.NOT_FOUND)
      .body(ErrorResponse(exception.message ?: "Resource not found"))
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
      .body(ErrorResponse(exception.message ?: "Request validation failed"))
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
      .body(ErrorResponse(exception.message ?: "Factory validation failed"))
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
      .body(ErrorResponse(exception.message ?: "Unexpected error"))
  }
}
