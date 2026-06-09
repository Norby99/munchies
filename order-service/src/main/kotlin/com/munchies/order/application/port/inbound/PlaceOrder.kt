package com.munchies.order.application.port.inbound

import com.munchies.order.application.port.inbound.command.PlaceOrderCommand
import com.munchies.order.infrastructure.adapter.dto.OrderDto

interface PlaceOrder {

  fun execute(command: PlaceOrderCommand): Result

  sealed interface Result {
    data class Success(val order: OrderDto) : Result
    sealed interface Failure : Result {
      data object InvalidDate : Failure
      data object EmptyItems : Failure
      data object InvalidItemQuantity : Failure
    }
  }
}
