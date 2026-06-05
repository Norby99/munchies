package com.munchies.order.application.port.inbound

import com.munchies.order.application.port.inbound.command.PlaceOrderCommand

interface PlaceOrder {

  fun execute(command: PlaceOrderCommand): Result

  sealed interface Result {
    data class Success(val orderId: String) : Result
    sealed interface Failure : Result {
      data object InvalidDate : Failure
      data object EmptyItems : Failure
    }
  }
}
