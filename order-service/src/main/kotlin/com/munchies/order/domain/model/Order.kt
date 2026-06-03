package com.munchies.order.domain.model

import com.munchies.commons.Entity

abstract class Order(
  override val id: OrderId,
  open val restaurantId: String,
  open val customerId: String,
) : Entity<OrderId>(id)
