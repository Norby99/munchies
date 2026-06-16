package com.munchies.order.application

import com.munchies.order.infrastructure.adapter.dto.OrderDto
import com.munchies.order.infrastructure.adapter.inbound.request.GetOrderDetailsRequest

interface GetOrderDetails {

  fun execute(command: GetOrderDetailsRequest): Result

  sealed interface Result {
    data class Success(val order: OrderDto) : Result
    sealed interface Failure : Result {
      data object OrderNotFound : Result
    }
  }
}
