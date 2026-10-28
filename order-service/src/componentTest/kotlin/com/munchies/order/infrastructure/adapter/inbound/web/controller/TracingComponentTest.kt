package com.munchies.order.infrastructure.adapter.inbound.web.controller

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import com.munchies.order.application.usecase.DiscardOrderUseCase
import com.munchies.order.fixtures.createDeliveryOrder
import com.munchies.order.fixtures.defaultOrderId
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.opentelemetry.api.trace.Span
import io.opentelemetry.context.Context
import io.opentelemetry.sdk.trace.ReadWriteSpan
import io.opentelemetry.sdk.trace.ReadableSpan
import io.opentelemetry.sdk.trace.SpanProcessor
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.CopyOnWriteArrayList
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

private const val SPEC_NAME = "TracingComponentTest"

/** Keeps every finished span in memory, standing in for the OTLP exporter. */
@Singleton
@Requires(property = "spec.name", value = SPEC_NAME)
class RecordingSpanProcessor : SpanProcessor {
  val finished = CopyOnWriteArrayList<ReadableSpan>()

  override fun onStart(parentContext: Context, span: ReadWriteSpan) = Unit

  override fun isStartRequired() = false

  override fun onEnd(span: ReadableSpan) {
    finished += span
  }

  override fun isEndRequired() = true
}

/**
 * Records, for every log line, the trace it was written in. Appenders run on the thread that
 * logs, which is also how the production appender finds the ids it adds to each line.
 */
private class TraceIdRecorder : AppenderBase<ILoggingEvent>() {
  val traceIds = CopyOnWriteArrayList<String>()

  override fun append(event: ILoggingEvent) {
    traceIds += Span.current().spanContext.traceId
  }
}

/**
 * Verifies the Distributed Tracing side of the Observability pattern: a request carrying a
 * `traceparent` header, as the ones forwarded by the gateway do, is served inside the caller's
 * trace rather than a new one, the lines logged while serving it belong to that trace, and
 * operational endpoints are left out of traces.
 */
@MicronautTest(environments = ["prod"], transactional = false)
@Property(name = "spec.name", value = SPEC_NAME)
class TracingComponentTest : BaseOrderController() {

  @Inject
  lateinit var spans: RecordingSpanProcessor

  // The JDK client is used on purpose: the Micronaut one is instrumented as well, and would
  // start a trace of its own instead of sending the header under test.
  private val http = HttpClient.newHttpClient()

  private fun send(method: String, path: String, traceparent: String? = null): Int {
    val request = HttpRequest.newBuilder(URI("http://localhost:${embeddedServer.port}$path"))
      .method(method, HttpRequest.BodyPublishers.noBody())
    traceparent?.let { request.header("traceparent", it) }
    return http.send(request.build(), HttpResponse.BodyHandlers.discarding()).statusCode()
  }

  private fun get(path: String, traceparent: String? = null) = send("GET", path, traceparent)

  @BeforeEach
  fun forgetSpans() = spans.finished.clear()

  @Test
  fun `a request carrying a traceparent header should continue the caller's trace`() {
    val traceId = "4bf92f3577b34da6a3ce929d0e0e4736"
    val callerSpanId = "00f067aa0ba902b7"

    get("/orders/unknown-order-id", traceparent = "00-$traceId-$callerSpanId-01")

    val serverSpan = spans.finished.single { it.parentSpanContext.spanId == callerSpanId }
    serverSpan.spanContext.traceId shouldBe traceId
  }

  @Test
  fun `lines logged while serving a request should belong to its trace`() {
    val traceId = "0af7651916cd43dd8448eb211c80319c"
    orderRepository.save(createDeliveryOrder())
    val recorder = TraceIdRecorder().apply { start() }
    val useCaseLogger = LoggerFactory.getLogger(DiscardOrderUseCase::class.java) as Logger
    useCaseLogger.addAppender(recorder)

    try {
      send(
        "DELETE",
        "/orders/${defaultOrderId.value}",
        "00-$traceId-b7ad6b7169203331-01",
      ) shouldBe 200
    } finally {
      useCaseLogger.detachAppender(recorder)
    }

    recorder.traceIds shouldBe listOf(traceId)
  }

  @Test
  fun `a request without a traceparent header should start a new trace`() {
    get("/orders/unknown-order-id")

    spans.finished.shouldNotBeEmpty()
  }

  @Test
  fun `health checks and metric scrapes should not be traced`() {
    get("/health")
    get("/prometheus")

    spans.finished.shouldBeEmpty()
  }
}
