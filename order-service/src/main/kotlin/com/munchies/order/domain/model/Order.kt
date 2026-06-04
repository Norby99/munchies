package com.munchies.order.domain.model

import com.munchies.commons.Entity

sealed class Order(
  override val id: OrderId,
  open val restaurantId: RestaurantId,
  open val customerId: CustomerId,
  open val items: List<OrderItem>,
) : Entity<OrderId>(id)

@JvmInline value class RestaurantId(val value: String)

@JvmInline value class CustomerId(val value: String)
