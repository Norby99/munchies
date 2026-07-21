package com.munchies.user.infrastructure.inbound.web.controller

import com.munchies.user.fixtures.HttpCallHelper
import com.munchies.user.infrastructure.adapter.dto.UserDTO
import com.munchies.user.infrastructure.adapter.inbound.request.*
import com.munchies.user.infrastructure.adapter.inbound.web.config.UserServiceConfig
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
@SerdeImport(GetUserResult::class)
@SerdeImport(GetUserRequest::class)
@SerdeImport(GetUserResponse::class)
@SerdeImport(GetUserFailure::class)
@SerdeImport(GetUserSuccess::class)
@SerdeImport(RegisterUserRequest::class)
@SerdeImport(RegisterUserResponse::class)
@SerdeImport(RegisterUserResult::class)
@SerdeImport(RegisterUserFailure::class)
@SerdeImport(RegisterUserSuccess::class)
@SerdeImport(LoginUserRequest::class)
@SerdeImport(LoginUserResponse::class)
@SerdeImport(LoginUserResult::class)
@SerdeImport(LoginUserFailure::class)
@SerdeImport(LoginUserSuccess::class)
@SerdeImport(GetUserResponse::class)
@SerdeImport(GetUserSuccess::class)
@SerdeImport(GetUserFailure::class)
@SerdeImport(UpdateUserInfoResponse::class)
@SerdeImport(UpdateUserInfoResult::class)
@SerdeImport(UpdateUserInfoRequest::class)
@SerdeImport(UpdateUserInfoSuccess::class)
@SerdeImport(UpdateUserInfoFailure::class)
@SerdeImport(UpdateUserPasswordResponse::class)
@SerdeImport(UpdateUserPasswordResult::class)
@SerdeImport(UpdateUserPasswordRequest::class)
@SerdeImport(UpdateUserPasswordSuccess::class)
@SerdeImport(UpdateUserPasswordFailure::class)
@SerdeImport(VerifyEmailResponse::class)
@SerdeImport(VerifyEmailResult::class)
@SerdeImport(VerifyEmailRequest::class)
@SerdeImport(VerifyEmailSuccess::class)
@SerdeImport(VerifyEmailFailure::class)
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
