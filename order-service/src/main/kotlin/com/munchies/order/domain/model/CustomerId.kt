package com.munchies.order.domain.model

import com.munchies.commons.UUIDEntityId

data class CustomerId(override val value: String = newId()) : UUIDEntityId(value)
