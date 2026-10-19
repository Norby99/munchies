package com.munchies.order.infrastructure.adapter.inbound.web.controller

import com.munchies.order.domain.model.OrderStatus
import com.munchies.order.fixtures.createAdvanceOrderStatusRequest
import com.munchies.order.fixtures.createDeliveryOrder
import com.munchies.order.fixtures.defaultOrderId
import com.munchies.order.infrastructure.adapter.inbound.web.config.OrderServiceConfig
import com.munchies.order.infrastructure.adapter.outbound.notification.orderstatuschanged.OrderStatusChangedNotificationInfo
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.junit.jupiter.api.Test
import org.testcontainers.kafka.KafkaContainer

/**
 * Verifies that a trace survives the asynchronous hop to notification-service: the Kafka
 * message published while serving a request carries that request's trace id in its
 * `traceparent` header, which is what lets the consumer continue the same trace.
 *
 * Unlike the other component tests, the notification publisher is not mocked and a real
 * broker is used, since the header is added by the Kafka client itself.
 */
@MicronautTest(environments = ["prod"], transactional = false)
class KafkaTracingComponentTest : BaseOrderController() {

  companion object {
    private const val POLL_ATTEMPTS = 30

    private val kafka: KafkaContainer by lazy {
      KafkaContainer("apache/kafka:4.3.1").apply { start() }
    }
  }

  override fun getProperties(): MutableMap<String, String> = super.getProperties().apply {
    put("kafka.bootstrap.servers", kafka.bootstrapServers)
  }

  @Test
  fun `a message published while serving a request should carry its trace`() {
    val traceId = "5b8aa5a2d2c872e8321cf37308d69df2"
    orderRepository.save(createDeliveryOrder(status = OrderStatus.PENDING))

    // The JDK client is used on purpose: the Micronaut one is instrumented as well, and would
    // start a trace of its own instead of sending the header under test.
    val request = HttpRequest
      .newBuilder(
        URI(
          "http://localhost:${embeddedServer.port}" +
            OrderServiceConfig.SERVICE_PATH + OrderServiceConfig.ADVANCE_ORDER_STATUS_PATH,
        ),
      )
      .header("Content-Type", "application/json")
      .header("traceparent", "00-$traceId-9d1f1e1c3a4b5c6d-01")
      .POST(
        HttpRequest.BodyPublishers.ofString(
          createAdvanceOrderStatusRequest(defaultOrderId).toJson(),
        ),
      )
      .build()
    HttpClient.newHttpClient().send(
      request,
      HttpResponse.BodyHandlers.discarding(),
    ).statusCode() shouldBe 200

    val traceparent = firstStatusChangedMessage().headers().lastHeader("traceparent")
    String(traceparent.value()) shouldStartWith "00-$traceId-"
  }

  private fun firstStatusChangedMessage(): ConsumerRecord<String, String> {
    val config = mapOf(
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to kafka.bootstrapServers,
      ConsumerConfig.GROUP_ID_CONFIG to "kafka-tracing-component-test",
      ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java.name,
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java.name,
    )
    KafkaConsumer<String, String>(config).use { consumer ->
      consumer.subscribe(listOf(OrderStatusChangedNotificationInfo.ORDER_STATUS_CHANGED_TOPIC))
      repeat(POLL_ATTEMPTS) {
        consumer.poll(Duration.ofSeconds(1)).firstOrNull()?.let { return it }
      }
    }
    error("No message was published on the order status-changed topic")
  }
}
