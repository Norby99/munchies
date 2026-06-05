package com.munchies.order.application.port.inbound

import com.munchies.order.application.port.inbound.command.UpdateDeliveryOrderCommand

interface UpdateDeliveryOrderInfo {

  fun execute(command: UpdateDeliveryOrderCommand): Result

  sealed interface Result {
    data class Success(val orderId: String) : Result
    sealed interface Failure : Result {
      data object DateInvalid : Result
    }
  }
}
