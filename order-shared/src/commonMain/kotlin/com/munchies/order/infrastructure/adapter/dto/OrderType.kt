package com.munchies.order.infrastructure.adapter.dto

import kotlin.js.JsExport
import kotlinx.serialization.Serializable

/**
 * Enum representing the different types of orders.
 *
 * This enum is used to specify whether an order is for delivery, takeaway, or dine-in.
 */
@JsExport
@Serializable
enum class OrderType {
  DELIVERY,
  TAKEAWAY,
  DINE_IN,
}
