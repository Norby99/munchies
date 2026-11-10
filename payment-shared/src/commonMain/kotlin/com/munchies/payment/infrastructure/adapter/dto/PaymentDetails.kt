package com.munchies.payment.infrastructure.adapter.dto

import kotlin.js.JsExport
import kotlinx.serialization.Serializable

/**
 * How the customer wants to pay.
 *
 * There is no amount nor currency: payment-service charges the order's total as stored by
 * order-service (priced from the restaurant's menus, in euro), so a client can never decide how
 * much it pays.
 *
 * @property method The payment method chosen by the customer.
 */
@JsExport
@Serializable
data class PaymentDetails(
  val method: PaymentMethod,
)
