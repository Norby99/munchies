package com.munchies.order.application

import com.munchies.order.infrastructure.adapter.inbound.request.AdvanceOrderStatusRequest

interface AdvanceOrderStatus {
  fun execute(command: AdvanceOrderStatusRequest): Result

  sealed interface Result {
    data object Success : Result
    sealed interface Failure : Result {
      data object OrderNotFound : Failure
      data object InvalidTransition : Failure
    }
  }
}
