package com.munchies.order.application.port.inbound

import com.munchies.order.application.port.inbound.command.UpdateTakeawayOrderCommand

interface UpdateTakeawayOrderInfo {

  fun execute(command: UpdateTakeawayOrderCommand): Result

  sealed interface Result {
    data class Success(val orderId: String) : Result
    sealed interface Failure : Result {
      data object OrderNotFound : Failure
      data object Unauthorized : Failure
      data object DateInvalid : Failure
    }
  }
}
