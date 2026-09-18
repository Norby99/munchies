package com.munchies.order.application.usecase

import com.munchies.order.application.port.inbound.AdvanceOrderStatus
import com.munchies.order.application.port.inbound.command.AdvanceOrderStatusCommand
import com.munchies.order.domain.model.Order
import com.munchies.order.domain.port.OrderNotificationPublisher
import com.munchies.order.domain.port.OrderRepository

/**
 * Use case implementation for advancing the status of an order.
 *
 * This class handles the business logic for transitioning an order to its next status
 * in the order processing workflow. It interacts with the OrderRepository to retrieve
 * and update order data, and publishes a status-change event once the transition succeeds.
 *
 * @property repository The repository used to access and modify order data.
 * @property notificationPublisher Publisher used to notify downstream consumers of the status change.
 */
class AdvanceOrderStatusUseCase(
  private val repository: OrderRepository,
  private val notificationPublisher: OrderNotificationPublisher,
) : AdvanceOrderStatus {

  override fun execute(command: AdvanceOrderStatusCommand): AdvanceOrderStatus.Result {
    val order = repository.findById(command.orderId)
      ?: return AdvanceOrderStatus.Result.Failure.OrderNotFound

    return when (val result = order.nextStatus()) {
      is Order.AdvanceStatusResult.Failure.InvalidTransition ->
        AdvanceOrderStatus.Result.Failure.InvalidTransition
      is Order.AdvanceStatusResult.Success -> {
        repository.update(result.order)
        notificationPublisher.publishStatusChanged(result.order)
        AdvanceOrderStatus.Result.Success
      }
    }
  }
}
