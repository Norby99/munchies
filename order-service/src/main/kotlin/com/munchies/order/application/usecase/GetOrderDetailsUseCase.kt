package com.munchies.order.application.usecase

import com.munchies.order.application.port.inbound.GetOrderDetails
import com.munchies.order.application.port.inbound.GetOrderDetails.Result.*
import com.munchies.order.application.port.inbound.GetOrderDetails.Result.Failure.*
import com.munchies.order.application.port.inbound.command.GetOrderDetailsCommand
import com.munchies.order.domain.port.OrderViewRepository
import com.munchies.order.infrastructure.adapter.dto.factory.OrderDtoFactory.toDto

/**
 * Query-side use case retrieving the details of a specific order.
 *
 * Reads from the [com.munchies.order.domain.model.view.OrderView] projection (CQRS query side),
 * never from the event store.
 *
 * @property views The repository of the order read model.
 */
class GetOrderDetailsUseCase(private val views: OrderViewRepository) : GetOrderDetails {
  override fun execute(command: GetOrderDetailsCommand): GetOrderDetails.Result =
    views.findById(command.orderId)?.let { Success(it.toDto()) } ?: OrderNotFound
}
