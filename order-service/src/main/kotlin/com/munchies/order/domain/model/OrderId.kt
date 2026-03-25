package com.munchies.order.domain.model

import com.munchies.commons.UUIDEntityId

<<<<<<< HEAD
data class OrderId(override val value: String = newId()) : UUIDEntityId(value)
=======
@JvmInline value class OrderId(val value: UUIDEntityId)

@JvmInline value class CustomerId(val value: UUIDEntityId)

@JvmInline value class RestaurantId(val value: UUIDEntityId)

@JvmInline value class MenuItemId(val value: UUIDEntityId)
>>>>>>> e938c0d (refactor(order): fix formatting error)
