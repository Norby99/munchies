package com.munchies.order.application.port.inbound

import com.munchies.order.application.port.inbound.command.AdvanceOrderStatusCommand

interface AdvanceOrderStatus {
  fun execute(command: AdvanceOrderStatusCommand): Result

  sealed interface Result {
    data object Success : Result
    sealed interface Failure : Result {
      data object OrderNotFound : Failure
      data object InvalidTransition : Failure
    }
  }
}
