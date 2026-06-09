package com.munchies.order.domain.factory

import com.munchies.order.domain.model.Order

sealed interface OrderCreationResult {
  data class Success(val order: Order) : OrderCreationResult
  sealed interface Failure : OrderCreationResult {
    data object EmptyItems : Failure
    data object InvalidItemQuantity : Failure
    data object InvalidDate : Failure
  }
}
