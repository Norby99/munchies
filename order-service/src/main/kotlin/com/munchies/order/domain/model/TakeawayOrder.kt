package com.munchies.order.domain.model

data class TakeawayOrder(
  override val id: OrderId,
  override val restaurantId: RestaurantId,
  override val customerId: CustomerId,
  override val status: OrderStatus,
  override val items: List<OrderItem>,
  val takeawayInfo: TakeawayInfo,
) : Order(id, restaurantId, customerId, status, items) {

  override fun nextStatus(): AdvanceStatusResult {
    val next = when (status) {
      OrderStatus.PENDING -> OrderStatus.PREPARING
      OrderStatus.PREPARING -> OrderStatus.READY
      OrderStatus.READY -> OrderStatus.COMPLETED
      else -> return AdvanceStatusResult.Failure.InvalidTransition
    }
    return AdvanceStatusResult.Success(copy(status = next))
  }

  override fun copyWithStatus(status: OrderStatus) = copy(status = status)

  fun updateInfo(pickupTime: Long, customerName: String): UpdateResult {
    val newInfo = TakeawayInfo(pickupTime, customerName)
    if (!newInfo.isValidTime()) return UpdateResult.Failure.InvalidDate

    return UpdateResult.Success(copy(takeawayInfo = newInfo))
  }

  sealed interface UpdateResult {
    data class Success(val order: TakeawayOrder) : UpdateResult
    sealed interface Failure : UpdateResult {
      data object InvalidDate : Failure
    }
  }
}

data class TakeawayInfo(
  val pickupTime: Long,
  val customerName: String,
) {
  fun isValidTime(): Boolean = pickupTime > System.currentTimeMillis()
}
