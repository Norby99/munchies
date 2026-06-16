package com.munchies.order.application

import com.munchies.order.infrastructure.adapter.dto.OrderDto
import com.munchies.order.infrastructure.adapter.inbound.request.PlaceOrderRequest

interface PlaceOrder {

  fun execute(command: PlaceOrderRequest): Result

  sealed interface Result {
    data class Success(val order: OrderDto) : Result
    sealed interface Failure : Result {
      data object InvalidDate : Failure
      data object EmptyItems : Failure
      data object InvalidItemQuantity : Failure
    }
  }
}
