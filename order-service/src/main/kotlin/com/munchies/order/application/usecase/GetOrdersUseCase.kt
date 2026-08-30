package com.munchies.order.application.usecase

import com.munchies.order.application.port.inbound.GetOrders
import com.munchies.order.application.port.inbound.command.GetOrdersCommand
import com.munchies.order.domain.ports.OrderRepository
import com.munchies.order.infrastructure.adapter.dto.factory.OrderDtoFactory.toDto

/**
 * Use case for retrieving orders based on the provided command.
 *
 * @property repository The repository used to access order data.
 */
class GetOrdersUseCase(private val repository: OrderRepository) : GetOrders {
  override fun execute(command: GetOrdersCommand): GetOrders.Result {
    var orders = repository.findAll()

    command.restaurantId?.let { resId -> orders = orders.filter { it.restaurantId == resId } }
    command.customerId?.let { cusId -> orders = orders.filter { it.customerId == cusId } }
    command.orderStatus?.let { status -> orders = orders.filter { it.status == status } }

    return GetOrders.Result.Success(orders.map { o -> o.toDto() })
  }
}
