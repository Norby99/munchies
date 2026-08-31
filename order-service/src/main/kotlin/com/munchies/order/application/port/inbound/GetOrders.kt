package com.munchies.order.application.port.inbound

import com.munchies.order.application.port.inbound.command.GetOrdersCommand
import com.munchies.order.infrastructure.adapter.dto.OrderDto

/**
 * Interface for retrieving orders based on the provided command.
 */
interface GetOrders {
  fun execute(command: GetOrdersCommand): Result

  /**
   * Sealed interface representing the result of the GetOrders operation.
   */
  sealed interface Result {
    data class Success(val orders: List<OrderDto>) : Result
    sealed interface Failure : Result {
      data object OrderNotFound : Failure
    }
  }
}
