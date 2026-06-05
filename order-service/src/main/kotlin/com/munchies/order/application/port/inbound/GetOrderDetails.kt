package com.munchies.order.application.port.inbound

import com.munchies.order.application.port.inbound.command.GetOrderDetailsCommand
import com.munchies.order.infrastructure.adapter.dto.OrderDto

interface GetOrderDetails {

  fun execute(command: GetOrderDetailsCommand): Result

  sealed interface Result {
    data class Success(val order: OrderDto) : Result
    sealed interface Failure : Result {
      data object DateInvalid : Result
    }
  }
}
