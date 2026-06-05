package com.munchies.order.application.port.inbound

import com.munchies.order.application.port.inbound.command.UpdateDeliveryOrderCommand

interface UpdateDeliveryOrderInfo {

  fun execute(command: UpdateDeliveryOrderCommand): Result

  sealed interface Result {
    data object Success : Result
    sealed interface Failure : Result {
      data object OrderNotFound : Failure
      data object Unauthorized : Failure
      data object InvalidDate : Failure
    }
  }
}
