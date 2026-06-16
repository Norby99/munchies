package com.munchies.order.application

import com.munchies.order.infrastructure.adapter.inbound.request.UpdateDeliveryOrderRequest

interface UpdateDeliveryOrderInfo {

  fun execute(command: UpdateDeliveryOrderRequest): Result

  sealed interface Result {
    data object Success : Result
    sealed interface Failure : Result {
      data object OrderNotFound : Failure
      data object Unauthorized : Failure
      data object InvalidDate : Failure
    }
  }
}
