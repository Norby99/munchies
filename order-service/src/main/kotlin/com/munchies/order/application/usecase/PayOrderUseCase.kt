package com.munchies.order.application.usecase

import com.munchies.order.application.eventsourcing.loadOrder
import com.munchies.order.application.port.inbound.PayOrder
import com.munchies.order.application.port.inbound.command.PayOrderCommand
import com.munchies.order.domain.model.Order
import com.munchies.order.domain.port.OrderEventStore
import com.munchies.order.domain.port.OrderNotificationPublisher
import org.slf4j.LoggerFactory

/**
 * Use case implementation for flagging an order as paid, by appending an
 * [com.munchies.order.domain.model.event.OrderPaid] event to its stream and, once it is durable,
 * notifying downstream consumers (the restaurant sees the order as paid).
 *
 * @property eventStore The event store holding the order streams.
 * @property notificationPublisher Publisher used to notify downstream consumers of the change.
 */
class PayOrderUseCase(
  private val eventStore: OrderEventStore,
  private val notificationPublisher: OrderNotificationPublisher,
) : PayOrder {
  override fun execute(command: PayOrderCommand): PayOrder.Result {
    val (order, version) = eventStore.loadOrder(command.orderId)
      ?: return PayOrder.Result.Failure.OrderNotFound

    return when (val result = order.pay()) {
      is Order.PayResult.Failure.AlreadyPaid -> {
        logger.warn("Order {} is already paid", order.id.value)
        PayOrder.Result.Failure.AlreadyPaid
      }
      is Order.PayResult.Success -> {
        eventStore.append(order.id, version, result.events)
        notificationPublisher.publishStatusChanged(order.applyAll(result.events))
        logger.info("Order {} marked as paid", order.id.value)
        PayOrder.Result.Success
      }
    }
  }

  private companion object {
    val logger = LoggerFactory.getLogger(PayOrderUseCase::class.java)
  }
}
