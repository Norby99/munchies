package com.munchies.order.application.usecase

import com.munchies.order.application.port.inbound.GetOrders
import com.munchies.order.application.port.inbound.command.GetOrdersCommand
import com.munchies.order.domain.port.OrderViewRepository
import com.munchies.order.infrastructure.adapter.dto.factory.OrderDtoFactory.toDto

/**
 * Query-side use case retrieving the orders matching the optional filters of the command.
 *
 * This is the query that motivates the CQRS read side: the event store can only be read by
 * order id, while the [com.munchies.order.domain.model.view.OrderView] projection is indexed by
 * restaurant, customer and status.
 *
 * @property views The repository of the order read model.
 */
class GetOrdersUseCase(private val views: OrderViewRepository) : GetOrders {
  override fun execute(command: GetOrdersCommand): GetOrders.Result {
    val orders = views.findBy(command.restaurantId, command.customerId, command.orderStatus)

    if (orders.isEmpty()) {
      return GetOrders.Result.Failure.OrderNotFound
    }
    return GetOrders.Result.Success(orders.map { it.toDto() })
  }
}
