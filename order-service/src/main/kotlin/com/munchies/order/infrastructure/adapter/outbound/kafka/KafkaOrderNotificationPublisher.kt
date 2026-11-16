package com.munchies.order.infrastructure.adapter.outbound.kafka

import com.munchies.order.domain.model.Order
import com.munchies.order.domain.port.OrderNotificationPublisher
import com.munchies.order.infrastructure.adapter.outbound.notification.orderstatuschanged.OrderStatusChangedNotification
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

/**
 * Kafka-backed implementation of [OrderNotificationPublisher], publishing order
 * status-change events for notification-service to consume.
 *
 * Publishing is best-effort: it runs after the order's events are durable, so a broker failure
 * must not turn a committed change into an error for the caller. A lost notification only means
 * a live update the frontend does not get (the data is still correct on the next load).
 */
@Singleton
class KafkaOrderNotificationPublisher(
  private val client: OrderStatusChangedKafkaClient,
) : OrderNotificationPublisher {

  override fun publishStatusChanged(order: Order) {
    runCatching {
      client.publish(
        OrderStatusChangedNotification(
          order_id_key = order.id.value,
          restaurant_id_key = order.restaurantId.value,
          customer_id_key = order.customerId.value,
          status_key = order.status.name,
          payed_key = order.payed,
        ).toJson(),
      )
    }.onFailure { logger.warn("Could not publish the change of order {}", order.id.value, it) }
  }

  private companion object {
    val logger = LoggerFactory.getLogger(KafkaOrderNotificationPublisher::class.java)
  }
}
