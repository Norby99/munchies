package com.munchies.order.domain.model

import com.munchies.commons.Entity

sealed class Order(
  override val id: OrderId,
  open val restaurantId: String,
  open val customerId: String,
  open val status: OrderStatus,
  open val items: List<OrderItem>,
) : Entity<OrderId>(id)
