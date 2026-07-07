package com.munchies.order.infrastructure.adapter.dto

import kotlinx.serialization.Serializable

@Serializable
data class OrderItemDto(val menuItemId: String, val quantity: Int)
