package com.munchies.order.application

import com.munchies.order.infrastructure.adapter.inbound.request.UpdateOrderItemsRequest

interface UpdateOrderItems {

  fun execute(command: UpdateOrderItemsRequest): Result

  sealed interface Result {
    data object Success : Result
    sealed interface Failure : Result {
      data object OrderNotFound : Failure
      data object Unauthorized : Failure
      data object EmptyItems : Failure
    }
  }
}
