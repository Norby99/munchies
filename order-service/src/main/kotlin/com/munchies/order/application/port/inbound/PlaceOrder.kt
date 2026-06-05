package com.munchies.order.application.port.inbound

import com.munchies.order.application.port.inbound.command.PlaceOrderCommand

interface PlaceOrder {

  fun execute(command: PlaceOrderCommand): Result

  sealed interface Result {
    data class Success(val orderId: String) : Result
    data object Failure : Result
  }
}
