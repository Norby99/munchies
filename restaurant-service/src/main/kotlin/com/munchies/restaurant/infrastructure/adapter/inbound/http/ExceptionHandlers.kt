package com.munchies.restaurant.infrastructure.adapter.inbound.http

import com.munchies.restaurant.infrastructure.adapter.inbound.http.exception.ConflictException
import com.munchies.restaurant.infrastructure.adapter.inbound.http.exception.UnauthorizedException
import com.munchies.restaurant.infrastructure.adapter.inbound.http.exception.ValidationException
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.server.exceptions.ExceptionHandler
import io.micronaut.http.server.exceptions.NotFoundException
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
    return HttpResponse.status<ErrorResponse>(HttpStatus.UNPROCESSABLE_ENTITY)
      .body(ErrorResponse(exception.message ?: "Validation error"))
  }
}

@Singleton
class ConflictExceptionHandler :
  ExceptionHandler<ConflictException, HttpResponse<ErrorResponse>> {
  override fun handle(
    request: HttpRequest<*>,
    exception: ConflictException,
  ): HttpResponse<ErrorResponse> {
    return HttpResponse.status<ErrorResponse>(HttpStatus.CONFLICT)
      .body(ErrorResponse(exception.message ?: "Conflict error"))
  }
}
