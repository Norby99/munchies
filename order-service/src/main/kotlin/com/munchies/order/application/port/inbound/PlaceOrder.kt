package com.munchies.order.application.port.inbound

import com.munchies.order.application.port.inbound.command.PlaceOrderCommand

interface PlaceOrder {

  fun execute(command: PlaceOrderCommand): CreateOrderResult

  sealed interface CreateOrderResult {
    data class Success(val orderId: String) : CreateOrderResult
    data object Failure : CreateOrderResult
  }
}
