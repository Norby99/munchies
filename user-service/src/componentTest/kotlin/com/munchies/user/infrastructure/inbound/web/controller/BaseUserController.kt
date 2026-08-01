package com.munchies.user.infrastructure.inbound.web.controller

import com.munchies.commons.domain.port.ValidationException
import com.munchies.commons.infrastructure.adapter.ErrorResponse
import com.munchies.user.fixtures.HttpCallHelper
import com.munchies.user.infrastructure.adapter.dto.UserDTO
import com.munchies.user.infrastructure.adapter.inbound.request.*
import com.munchies.user.infrastructure.adapter.inbound.web.config.UserServiceConfig
import com.munchies.user.infrastructure.adapter.inbound.web.controller.exception.FactoryException
import com.munchies.user.infrastructure.adapter.inbound.web.controller.exception.NotFoundException
import com.munchies.user.infrastructure.adapter.inbound.web.controller.exception.UnauthorizedException
import com.munchies.user.infrastructure.adapter.inbound.web.controller.exception.UnexpectedException
import com.munchies.user.infrastructure.adapter.outbound.response.*
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.serde.ObjectMapper
import io.micronaut.serde.annotation.SerdeImport
import io.micronaut.test.support.TestPropertyProvider
import jakarta.inject.Inject
import org.junit.jupiter.api.TestInstance
import org.testcontainers.mongodb.MongoDBContainer
@SerdeImport(UserDTO::class)
@SerdeImport(GetUserRequest::class)
@SerdeImport(GetUserResponse::class)
@SerdeImport(RegisterUserRequest::class)
@SerdeImport(RegisterUserResponse::class)
@SerdeImport(LoginUserRequest::class)
@SerdeImport(LoginUserResponse::class)
@SerdeImport(LoginUserResult::class)
@SerdeImport(UpdateUserInfoRequest::class)
@SerdeImport(UpdateUserInfoResponse::class)
@SerdeImport(UpdateUserPasswordRequest::class)
@SerdeImport(UpdateUserPasswordResponse::class)
@SerdeImport(VerifyEmailRequest::class)
@SerdeImport(VerifyEmailResponse::class)
@SerdeImport(DeleteUserRequest::class)
@SerdeImport(DeleteUserResponse::class)
@SerdeImport(ErrorResponse::class)
@SerdeImport(FactoryException::class)
@SerdeImport(NotFoundException::class)
@SerdeImport(UnauthorizedException::class)
@SerdeImport(UnexpectedException::class)
@SerdeImport(ValidationException::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseUserController : TestPropertyProvider {

  companion object {
    private val mongo: MongoDBContainer by lazy {
      MongoDBContainer("mongo:7.0").apply { start() }
    }
  }

  override fun getProperties(): MutableMap<String, String> = mutableMapOf(
    "mongodb.uri" to "${mongo.connectionString}/user-service",
    "mongodb.package-names[0]" to
      "com.munchies.user.infrastructure.adapter.outbound.mongo.document",
  )

  @Inject
  @field:Client("/")
  lateinit var client: HttpClient

  @Inject
  lateinit var mapper: ObjectMapper

  @Inject
  lateinit var embeddedServer: EmbeddedServer

  val httpCalls: HttpCallHelper by lazy { HttpCallHelper(baseUrl(), client) }

  private fun baseUrl(): String =
    "http://localhost:${embeddedServer.port}${UserServiceConfig.SERVICE_PATH}"
}
