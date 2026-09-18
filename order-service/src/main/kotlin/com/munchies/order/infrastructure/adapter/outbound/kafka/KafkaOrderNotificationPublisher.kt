package com.munchies.order.infrastructure.adapter.outbound.kafka

import com.munchies.order.domain.model.Order
import com.munchies.order.domain.port.OrderNotificationPublisher
import com.munchies.order.infrastructure.adapter.outbound.notification.OrderStatusChangedNotification
import jakarta.inject.Singleton

/**
 * Kafka-backed implementation of [OrderNotificationPublisher], publishing order
 * status-change events for notification-service to consume.
 */
@Singleton
class KafkaOrderNotificationPublisher(
  private val client: OrderStatusChangedKafkaClient,
) : OrderNotificationPublisher {

  override fun publishStatusChanged(order: Order) {
    client.publish(
      OrderStatusChangedNotification(
        order_id_key = order.id.value,
        restaurant_id_key = order.restaurantId.value,
        customer_id_key = order.customerId.value,
        status_key = order.status.name,
      ).toJson(),
    )
  }
}
