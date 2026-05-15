package com.munchies.order

import io.micronaut.configuration.kafka.annotation.KafkaClient
import io.micronaut.configuration.kafka.annotation.KafkaListener
import io.micronaut.configuration.kafka.annotation.OffsetReset
import io.micronaut.configuration.kafka.annotation.Topic
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.context.event.StartupEvent
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@KafkaClient
interface OrderClient {
  @Topic("temp-orders")
  fun sendOrder(order: String)
}

@KafkaListener(offsetReset = OffsetReset.EARLIEST)
class OrderConsumer {
  private val logger = LoggerFactory.getLogger(OrderConsumer::class.java)

  @Topic("temp-orders")
  fun receiveOrder(order: String) {
    logger.info("\n+++ CONSUMED MESSAGE FROM KAFKA TOPIC 'temp-orders' +++")
    logger.info("Payload: {}\n", order)
  }
}

@Singleton
class StartupProducer(
  private val orderClient: OrderClient,
) : ApplicationEventListener<StartupEvent> {
  private val logger = LoggerFactory.getLogger(StartupProducer::class.java)

  override fun onApplicationEvent(event: StartupEvent) {
    logger.info("Sending temporary order to Kafka...")
    @Suppress("TooGenericExceptionCaught")
    try {
      orderClient.sendOrder(
        "Test order from order-service prototype: ${System.currentTimeMillis()}",
      )
      logger.info("Order sent successfully!")
    } catch (e: Exception) {
      logger.error("Failed to send order to Kafka: ${e.message}", e)
    }
  }
}
