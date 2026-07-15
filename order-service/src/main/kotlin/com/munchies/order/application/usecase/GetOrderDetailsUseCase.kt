package com.munchies.order.application.usecase

import com.munchies.order.application.port.inbound.GetOrderDetails
import com.munchies.order.application.port.inbound.GetOrderDetails.Result.*
import com.munchies.order.application.port.inbound.GetOrderDetails.Result.Failure.*
import com.munchies.order.application.port.inbound.command.GetOrderDetailsCommand
import com.munchies.order.domain.ports.OrderRepository
import com.munchies.order.infrastructure.adapter.dto.factory.OrderDtoFactory.toDto

/**
 * Use case implementation for retrieving the details of a specific order.
 *
 * This class handles the business logic for fetching order details based on the provided order ID.
 * It interacts with the OrderRepository to retrieve order data and convert it to a DTO format.
 *
 * @property repository The repository used to access order data.
 */
class GetOrderDetailsUseCase(private val repository: OrderRepository) : GetOrderDetails {
  override fun execute(command: GetOrderDetailsCommand): GetOrderDetails.Result {
    val order = repository.findById(command.orderId)
    return if (order != null) {
      Success(order.toDto())
    } else {
      OrderNotFound
    }
  }
}
