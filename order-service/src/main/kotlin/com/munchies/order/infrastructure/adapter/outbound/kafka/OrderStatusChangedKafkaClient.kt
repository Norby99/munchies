package com.munchies.order.infrastructure.adapter.outbound.kafka

import com.munchies.order.infrastructure.adapter.outbound.notification.OrderStatusChangedNotificationInfo
import io.micronaut.configuration.kafka.annotation.KafkaClient
import io.micronaut.configuration.kafka.annotation.Topic
import io.micronaut.serde.annotation.SerdeImport

/**
 * Kafka client used to publish order status-change events for downstream consumers.
 */
@SerdeImport
@KafkaClient
interface OrderStatusChangedKafkaClient {
  @Topic(OrderStatusChangedNotificationInfo.ORDER_STATUS_CHANGED_TOPIC)
  fun publish(notification: String)
}
