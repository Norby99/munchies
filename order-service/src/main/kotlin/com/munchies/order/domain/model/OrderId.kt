package com.munchies.order.domain.model

import com.munchies.commons.UUIDEntityId

@JvmInline value class OrderId(val value: UUIDEntityId)
@JvmInline value class CustomerId(val value: UUIDEntityId)
@JvmInline value class RestaurantId(val value: UUIDEntityId)
@JvmInline value class MenuItemId(val value: UUIDEntityId)
