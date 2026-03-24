package com.munchies.order.domain.model

import com.munchies.commons.UUIDEntityId

data class RestaurantId(override val value: String = newId()) : UUIDEntityId(value)
