package com.munchies.order.application.port.inbound

import com.munchies.order.application.port.inbound.command.UpdateOrderStatusCommand

interface UpdateOrderStatus {
  fun execute(command: UpdateOrderStatusCommand): Result

  sealed interface Result {
    data object Success : Result
    sealed interface Failure : Result {
      data object OrderNotFound : Failure
      data object InvalidStatusTransition : Failure
    }
  }
}
