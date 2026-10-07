package com.munchies.order.application.usecase

import com.munchies.order.application.eventsourcing.loadOrder
import com.munchies.order.application.port.inbound.AdvanceOrderStatus
import com.munchies.order.application.port.inbound.command.AdvanceOrderStatusCommand
import com.munchies.order.domain.model.Order
import com.munchies.order.domain.port.OrderEventStore
import com.munchies.order.domain.port.OrderNotificationPublisher
import org.slf4j.LoggerFactory

/**
 * Use case implementation for advancing the status of an order.
 *
 * Rebuilds the order from its event stream, lets the aggregate decide the resulting
 * [com.munchies.order.domain.model.event.OrderStatusAdvanced] event, appends it to the event store and,
 * once it is durable, publishes a status-change notification for notification-service.
 *
 * @property eventStore The event store holding the order streams.
 * @property notificationPublisher Publisher used to notify downstream consumers of the status change.
 */
class AdvanceOrderStatusUseCase(
  private val eventStore: OrderEventStore,
  private val notificationPublisher: OrderNotificationPublisher,
) : AdvanceOrderStatus {

  override fun execute(command: AdvanceOrderStatusCommand): AdvanceOrderStatus.Result {
    val (order, version) = eventStore.loadOrder(command.orderId)
      ?: return AdvanceOrderStatus.Result.Failure.OrderNotFound

    return when (val result = order.nextStatus()) {
      is Order.AdvanceStatusResult.Failure.InvalidTransition -> {
        logger.warn("Order {} cannot advance from status {}", order.id.value, order.status)
        AdvanceOrderStatus.Result.Failure.InvalidTransition
      }
      is Order.AdvanceStatusResult.Success -> {
        val advanced = order.applyAll(result.events)
        eventStore.append(order.id, version, result.events)
        notificationPublisher.publishStatusChanged(advanced)
        logger.info(
          "Order {} advanced from {} to {}",
          order.id.value,
          order.status,
          advanced.status,
        )
        AdvanceOrderStatus.Result.Success
      }
    }
  }

  private companion object {
    val logger = LoggerFactory.getLogger(AdvanceOrderStatusUseCase::class.java)
  }
}
