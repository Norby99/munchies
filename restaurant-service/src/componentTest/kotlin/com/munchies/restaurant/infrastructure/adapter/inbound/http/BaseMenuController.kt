package com.munchies.restaurant.infrastructure.adapter.inbound.http

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.serde.ObjectMapper
import io.micronaut.test.support.TestPropertyProvider
import jakarta.inject.Inject
import org.junit.jupiter.api.TestInstance
import org.testcontainers.mongodb.MongoDBContainer

/**
 * Shared scaffolding for restaurant-service component tests: boots a real embedded
 * Micronaut server backed by a Testcontainers MongoDB instance, and exposes a small
 * HTTP client helper so subclasses can drive controllers exactly as a real caller would.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseMenuController : TestPropertyProvider {

  companion object {
    private val mongo: MongoDBContainer by lazy {
      MongoDBContainer("mongo:7.0").apply { start() }
    }
  }

  override fun getProperties(): MutableMap<String, String> = mutableMapOf(
    "mongodb.uri" to "${mongo.connectionString}/restaurant-service",
    "mongodb.package-names[0]" to
      "com.munchies.restaurant.infrastructure.adapter.outbound.mongo.document",
  )

  @Inject
  @field:Client("/")
  lateinit var client: HttpClient

  @Inject
  lateinit var mapper: ObjectMapper

  @Inject
  lateinit var embeddedServer: EmbeddedServer

  inline fun <reified T> HttpResponse<*>.bd() = this.getBody(T::class.java).get()

  inline fun <reified T> httpPost(path: String, request: Any): HttpResponse<T> =
    client.toBlocking().exchange(HttpRequest.POST("${baseUrl()}$path", request), T::class.java)

  inline fun <reified T> httpGet(path: String): HttpResponse<T> =
    client.toBlocking().exchange(HttpRequest.GET<Any>("${baseUrl()}$path"), T::class.java)

  inline fun <reified T> httpPut(path: String, request: Any): HttpResponse<T> =
    client.toBlocking().exchange(HttpRequest.PUT("${baseUrl()}$path", request), T::class.java)

  inline fun <reified T> httpDelete(path: String): HttpResponse<T> =
    client.toBlocking().exchange(HttpRequest.DELETE<Any>("${baseUrl()}$path"), T::class.java)

  inline fun <reified T> httpDelete(path: String, request: Any): HttpResponse<T> =
    client.toBlocking().exchange(HttpRequest.DELETE("${baseUrl()}$path", request), T::class.java)

  /**
   * @return the base URL of the embedded server, without any service-specific path
   * since restaurant-service's controller paths are parameterized per-request
   * (e.g. `/restaurant/{restaurantId}/menus/{menuId}/...`).
   */
  fun baseUrl(): String = "http://localhost:${embeddedServer.port}"
}
