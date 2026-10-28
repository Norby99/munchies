package com.munchies.order.infrastructure.adapter.inbound.web.controller

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Verifies the Application Metrics side of the Observability pattern: the service exposes its
 * Micrometer registry in the Prometheus text format, and HTTP traffic shows up in it as the
 * `http_server_requests_seconds` histogram the dashboards are built on.
 */
@MicronautTest(environments = ["prod"], transactional = false)
class MetricsEndpointComponentTest : BaseOrderController() {

  @Test
  fun `GET prometheus should expose the request latency histogram after serving traffic`() {
    // Any handled request is enough to register the timer, a 404 included.
    assertThrows<HttpClientResponseException> {
      httpCalls.httpGet<String>("/unknown-order-id")
    }

    val response = client.toBlocking().exchange(
      HttpRequest.GET<Any>("http://localhost:${embeddedServer.port}/prometheus"),
      String::class.java,
    )

    response.status shouldBe HttpStatus.OK
    response.body() shouldContain "http_server_requests_seconds_bucket"
    response.body() shouldContain "jvm_memory_used_bytes"
  }
}
