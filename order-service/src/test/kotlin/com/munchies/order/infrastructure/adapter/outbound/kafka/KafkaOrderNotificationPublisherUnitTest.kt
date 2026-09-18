package com.munchies.order.infrastructure.adapter.outbound.kafka

import com.munchies.order.domain.model.OrderStatus
import com.munchies.order.fixtures.createDeliveryOrder
import com.munchies.order.fixtures.defaultCustomerId
import com.munchies.order.fixtures.defaultOrderId
import com.munchies.order.fixtures.defaultRestaurantId
import com.munchies.order.infrastructure.adapter.outbound.notification.orderStatusChangedNotificationFromJson
import io.kotest.matchers.equals.shouldBeEqual
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test

class KafkaOrderNotificationPublisherUnitTest {

  private val client = mockk<OrderStatusChangedKafkaClient>(relaxed = true)
  private val publisher = KafkaOrderNotificationPublisher(client)

  @Test
  fun `publishStatusChanged should publish a notification carrying the order's full status`() {
    val order = createDeliveryOrder(status = OrderStatus.PREPARING)
    val payload = slot<String>()

    every { client.publish(capture(payload)) } returns Unit

    publisher.publishStatusChanged(order)

    verify(exactly = 1) { client.publish(any()) }
    val notification = orderStatusChangedNotificationFromJson(payload.captured)
    notification.order_id_key shouldBeEqual defaultOrderId.value
    notification.restaurant_id_key shouldBeEqual defaultRestaurantId.value
    notification.customer_id_key shouldBeEqual defaultCustomerId.value
    notification.status_key shouldBeEqual OrderStatus.PREPARING.name
  }
}
