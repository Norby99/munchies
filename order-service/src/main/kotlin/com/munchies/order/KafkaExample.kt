package com.munchies.order

import io.micronaut.configuration.kafka.annotation.KafkaClient
import io.micronaut.configuration.kafka.annotation.KafkaListener
import io.micronaut.configuration.kafka.annotation.OffsetReset
import io.micronaut.configuration.kafka.annotation.Topic
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.context.event.StartupEvent
import jakarta.inject.Singleton

@KafkaClient
interface OrderClient {
  @Topic("temp-orders")
  fun sendOrder(order: String)
}

@KafkaListener(offsetReset = OffsetReset.EARLIEST)
class OrderConsumer {
  @Topic("temp-orders")
  fun receiveOrder(order: String) {
    println("\n+++ CONSUMED MESSAGE FROM KAFKA TOPIC 'temp-orders' +++")
    println("Payload: $order\n")
  }
}

@Singleton
class StartupProducer(
  private val orderClient: OrderClient,
) : ApplicationEventListener<StartupEvent> {
  override fun onApplicationEvent(event: StartupEvent) {
    println("Sending temporary order to Kafka...")
    try {
      orderClient.sendOrder(
        "Test order from order-service prototype: ${System.currentTimeMillis()}",
      )
      println("Order sent successfully!")
    } catch (_: Exception) {
      println("Failed to write to Kafka. Please check connection.")
    }
  }
}
